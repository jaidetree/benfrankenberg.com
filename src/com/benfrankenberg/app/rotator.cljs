(ns com.benfrankenberg.app.rotator
  (:require
   [com.benfrankenberg.app.dom :refer [swap-class! toggle-class!]]
   [com.benfrankenberg.app.state :refer [action? bus create-store gen-action]]
   [com.benfrankenberg.app.stream :as stream]
   [com.benfrankenberg.app.util :refer [query query-all]]))

(def bacon (.-Bacon js/window))

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

(defn next-idx
  [idx total]
  (if (> idx total) 1 idx))

(defn prev-idx
  [idx total]
  (if (<= idx 0) total idx))

(defn prepare-transition
  [{:keys [from-el to-el direction-cls]}]
  (toggle-class! from-el "transition" "from" direction-cls)
  (toggle-class! to-el   "transition" "to"   direction-cls))

(defn start-transition
  [{:keys [from-el to-el]}]
  (toggle-class! from-el "rotate")
  (toggle-class! to-el   "rotate"))

(defn end-transition
  [{:keys [from-el to-el direction-cls]}]
  (toggle-class! from-el "transition" "from" "rotate" direction-cls)
  (toggle-class! to-el   "transition" "to"   "rotate" direction-cls))

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
        (.delay 1200)
        (.doAction end-transition)
        (.doAction #(swap-class! container to-el "active"))
        (.map (constantly to)))))

;; Effects
;; ---------------------------------------------------------------------------

(defn next-slide
  [actions state]
  (-> actions
      (action? :next)
      (.flatMapLatest #(.take state 1))
      (.map (fn [{:keys [total current] :as db}]
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
      (.map (fn [{:keys [current total] :as db}]
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

(def fx
  [next-slide
   prev-slide
   rotate])

(defn ui-events
  [container]
  (-> #js [(.fromEvent bacon (query container ".next") "click")
           (.fromEvent bacon (query container ".prev") "click")]
      (->> (.mergeAll bacon))
      (.map #(-> % (.-currentTarget) (.-value)))))

;; Public API
;; ---------------------------------------------------------------------------

(defn rotator
  [selector]
  (let [container (query selector)
        slides (query-all container ".slide")
        index (or (query-initial-index slides) 1)
        {:keys [dispatch state] :as store} (create-store {:current index} reducers fx)]
    (dispatch {:type :start :data {:total (count slides)
                                   :selector selector
                                   :current 1}})
    (-> (ui-events container)
        (.takeUntil bus)
        (.onValue #(dispatch {:type (keyword %) :data nil})))
    store))
