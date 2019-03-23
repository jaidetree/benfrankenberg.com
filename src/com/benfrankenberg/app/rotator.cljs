(ns com.benfrankenberg.app.rotator
  (:require
   [clojure.string :refer [join split]]
   [com.benfrankenberg.app.raf :refer [delay-frame next-frame]]
   [com.benfrankenberg.app.state :refer [action? bus create-store gen-action]]
   [com.benfrankenberg.app.util :refer [query query-all with-latest-from]]))

(def bacon (.-Bacon js/window))
(def Bus (.-Bus bacon))

(defn stream-of
  [source]
  (.once bacon source))

(defn stream-from
  [source]
  (cond (sequential? source)       (stream-from (clj->js source))
        (.isArray js/Array source) (.fromArray bacon source)
        :else                      (stream-from [source])))

"How should the rotator work?
  - Call (rotator \".rotator\")
  - Create a property stream to store state
    - {:current 1
       :status :transition
       :from 1
       :to 2}
    - Initial index is .active"

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

(defn el->classes
  [el]
  (-> (.-className el)
      (split #" ")
      (set)))

(defn toggle-class!
  [el & class-names]
  (let [classes (reduce (fn [classes class-name]
                          (if (contains? classes class-name)
                            (disj classes class-name)
                            (conj classes class-name)))
                        (el->classes el)
                        class-names)]
    (set! (.-className el) (join " " classes))
    el))

(defn remove-classes!
  [el target-classes]
  (set! (.-className el)
        (->> el
             (el->classes)
             (remove (set target-classes))
             (join " "))))

(defn add-classes!
  [el target-classes]
  (set! (.-className el)
        (->> el
             (el->classes)
             (into (set target-classes))
             (join " "))))

(defn swap-class!
  [container el & class-names]
  (let [target-classes (str "." (join "." class-names))]
    (-> (query-all container target-classes)
        (stream-from)
        (.onValue #(remove-classes! % class-names)))
    (add-classes! el class-names)
    el))

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
    (-> (.once bacon state)
        (.map #(merge % {:from-el from-el
                         :to-el to-el
                         :direction-cls (name direction)}))
        (.doAction prepare-transition)
        (delay-frame)
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
