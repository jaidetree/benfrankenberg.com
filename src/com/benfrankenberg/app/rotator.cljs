(ns com.benfrankenberg.app.rotator
  (:require
   [clojure.string :refer [join split]]
   [com.benfrankenberg.app.raf :refer [delay-frame next-frame]]
   [com.benfrankenberg.app.state :refer [action? bus create-store gen-action]]
   [com.benfrankenberg.app.util :refer [query query-all with-latest-from]]))

(def bacon (.-Bacon js/window))
(def Bus (.-Bus bacon))

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

(def reducers {:rotated      (fn [db {:keys [data]}]
                               (merge db data))
               :count-slides (fn [db {:keys [data]}]
                               (assoc db :total data))
               :rotate       (fn [db {:keys [data]}]
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



(defn toggle-class
  [el & class-names]
  (let [classes (reduce (fn [classes class-name]
                          (if (contains? (set classes) class-name)
                            (->> classes
                                (remove #(= % class-name)))
                            (-> classes
                                (conj class-name))))
                        (split (.-className el) #" ")
                        class-names)]
    (set! (.-className el) (join " " classes))
    el))

(defn prepare-transition
  [{:keys [from-el to-el direction-cls]}]
  (toggle-class from-el "transition" "from" direction-cls)
  (toggle-class to-el   "transition" "to"   direction-cls))

(defn start-transition
  [{:keys [from-el to-el]}]
  (toggle-class from-el "rotate")
  (toggle-class to-el   "rotate"))

(defn end-transition
  [{:keys [from-el to-el direction-cls]}]
  (toggle-class from-el "transition" "from" "rotate" direction-cls)
  (toggle-class to-el   "transition" "to"   "rotate" direction-cls))

(defn rotate-slide-elements
  [{:keys [direction from to] :as state}]
  (let [from-el (query (str ".slide[data-id=\"" from "\"]"))
        to-el   (query (str ".slide[data-id=\"" to "\"]"))]
    (-> (.once bacon state)
        (.map #(merge % {:from-el from-el
                         :to-el to-el
                         :direction-cls (name direction)}))
        (.doAction prepare-transition)
        (delay-frame)
        (.doAction start-transition)
        (.delay 1200)
        (.doAction end-transition)
        (.map state))))

;; Effects
;; ---------------------------------------------------------------------------

(defn next-slide
  [actions state]
  (-> actions
      (action? :next)
      (with-latest-from state)
      (.map (fn [[_ {:keys [total current]}]]
              (let [idx (next-idx (inc current) total)]
                (merge  {:type :rotate
                         :data {:current idx
                                :direction :forwards
                                :from current
                                :to idx}}))))))

(defn prev-slide
  [actions state]
  (-> actions
      (action? :prev)
      (with-latest-from state)
      (.map (fn [[_ {:keys [current total]}]]
              (let [idx (prev-idx (dec current) total)]
                (merge  {:type :rotate
                         :data {:current idx
                                :direction :backwards
                                :from current
                                :to idx}}))))))

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
    (dispatch {:type :count-slides :data (count slides)})
    (-> (ui-events container)
        (.takeUntil bus)
        (.onValue #(dispatch {:type (keyword %) :data nil})))
    store))
