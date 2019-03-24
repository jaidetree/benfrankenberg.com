(ns com.benfrankenberg.app.rotator
  (:require
   [bacon :as bacon]
   [com.benfrankenberg.app.dom :as dom]
   [com.benfrankenberg.app.state :refer [action? bus create-store gen-action]]
   [com.benfrankenberg.app.stream :as stream]
   [com.benfrankenberg.app.swipe :refer [swipe]]
   [com.benfrankenberg.app.util :refer [query query-all]]))

;; Reducers
;; ---------------------------------------------------------------------------

(def reducers {:rotate       (fn [db {:keys [data]}]
                               (merge db data))
               :rotated      (fn [db {:keys [data]}]
                               (merge db {:current data
                                          :from nil
                                          :to nil}))
               :start        (fn [db {:keys [data]}]
                               (merge db data))})

;; Utils
;; ---------------------------------------------------------------------------

(defn query-initial-index
  [slides]
  (some->> slides
           (map-indexed vector)
           (filter (fn [[_ el]] (.includes (.-className el) "active")))
           (first)
           (first)))

(defn query-container
  [action]
  (query (get-in action [:data :selector])))

(defn scale-btn
  [{:keys [direction el scale selector]}]
  (let [btn (query el selector)
        style (.-style btn)
        size (+ 1 (* scale 0.5))
        opacity (+ 0.5 (* scale 0.5))]
    (set! (.-transform style)
          (str "scale(" size ")"))
    (set! (.-opacity style)
          opacity)))

(defn reset-btn-scale
  [{:keys [el]}]
  (doseq [selector [".prev" ".next"]]
    (let [btn (query el selector)
          style (.-style btn)]
      (set! (.-transform style)
            "scale(1)")
      (set! (.-opacity style)
            ""))))

(defn next-idx
  [idx total]
  (if (> idx total) 1 idx))

(defn prev-idx
  [idx total]
  (if (<= idx 0) total idx))

(defn prepare-transition
  [{:keys [from-el to-el direction-cls]}]
  (dom/toggle-class! from-el "transition" "from" direction-cls)
  (dom/toggle-class! to-el   "transition" "to"   direction-cls))

(defn start-transition
  [{:keys [from-el to-el]}]
  (dom/toggle-class! from-el "rotate")
  (dom/toggle-class! to-el   "rotate"))

(defn end-transition
  [{:keys [from-el to-el direction-cls]}]
  (dom/toggle-class! from-el "transition" "from" "rotate" direction-cls)
  (dom/toggle-class! to-el   "transition" "to"   "rotate" direction-cls))

(defn rotate-slide-elements
  [{:keys [direction from selector to] :as state}]
  (let [container (query selector)
        from-el (query container (str ".slide[data-id=\"" from "\"]"))
        to-el   (query container (str ".slide[data-id=\"" to "\"]"))]
    (-> (stream/of state)
        (.map #(merge % {:from-el from-el
                         :to-el to-el
                         :direction-cls (name direction)}))
        (.doAction prepare-transition)
        (stream/delay-frame)
        (.doAction start-transition)
        (.delay 800)
        (.doAction end-transition)
        (.doAction #(dom/swap-class! container to-el "active"))
        (.map (constantly to)))))

;; Effects
;; ---------------------------------------------------------------------------

(defn button-events
  [actions state]
  (-> actions
      (action? :start)
      (.map query-container)
      (.flatMap
       (fn [container]
         (-> [(query container ".prev")
              (query container ".next")]
             (stream/from)
             (.flatMap #(.fromEvent bacon % "click"))
             (.map #(-> % (.-currentTarget) (.-value)))
             (.map #(gen-action (keyword %) (str "." %))))))))

(defn button-fx
  [actions state]
  (-> actions
      (action? :prev :next)
      (stream/with-latest-from state)
      (.map (fn [[{button-selector :data} {:keys [selector]}]]
              (let [container (query selector)]
                (query container button-selector))))
      (.flatMap (fn [el]
                  (-> (stream/of el)
                      (stream/delay-frame)
                      (.doAction #(dom/add-classes! % ["active"]))
                      (.delay 500)
                      (.doAction #(dom/remove-classes! % ["active"])))))
      (.filter false)))

(defn next-slide
  [actions state]
  (-> actions
      (action? :next)
      (.flatMapLatest #(.take state 1))
      (.map
       (fn [{:keys [total current] :as db}]
         (let [idx (next-idx (inc current) total)]
           (merge  {:type :rotate
                    :data (assoc db :current idx
                                    :from current
                                    :to idx
                                    :direction "forwards")}))))))

(defn prev-slide
  [actions state]
  (-> actions
      (action? :prev)
      (.flatMapLatest #(.take state 1))
      (.map
       (fn [{:keys [current total] :as db}]
         (let [idx (prev-idx (dec current) total)]
           (merge  {:type :rotate
                    :data (assoc db :current idx
                                    :from current
                                    :to idx
                                    :direction "backwards")}))))))

(defn rotate
  [actions state]
  (-> actions
      (action? :rotate)
      (.map #(get % :data))
      (.flatMapConcat rotate-slide-elements)
      (.map (gen-action :rotated))))

(defn swipe-events
  [actions state]
  (-> actions
      (action? :start)
      (.map query-container)
      (.flatMap #(swipe {:el % :on-move scale-btn :on-end reset-btn-scale}))
      (.map #(gen-action (:direction %) (:selector %)))))

(def fx
  [button-events
   button-fx
   next-slide
   prev-slide
   rotate
   swipe-events])

;; Public API
;; ---------------------------------------------------------------------------

(defn rotator
  [selector]
  (let [container (query selector)
        slides (query-all container ".slide")
        index (or (query-initial-index slides) 1)
        store (create-store {:current index} reducers fx)
        {:keys [dispatch state]} store]
    (dispatch {:type :start
               :data {:total (count slides)
                      :selector selector
                      :current 1}})
    store))
