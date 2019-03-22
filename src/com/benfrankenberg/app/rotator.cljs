(ns com.benfrankenberg.app.rotator
  (:require
   [com.benfrankenberg.app.state :refer [bus create-store action?]]
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

;; Utils
;; ---------------------------------------------------------------------------

(def reducers {:rotated      (fn [db action]
                               (assoc db :current (:data action)))
               :count-slides (fn [db {:keys [data]}]
                               (assoc db :total data))
               :rotate       (fn [db {:keys [data]}]
                               (merge db data))})

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

;; Next
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
  [])

(def epics [next-slide
            prev-slide])

(defn ui-events
  [container]
  (-> #js [(.fromEvent bacon (query container ".next") "click")
           (.fromEvent bacon (query container ".prev") "click")]
      (->> (.mergeAll bacon))
      (.map #(-> % (.-currentTarget) (.-value)))))

(defn rotator
  [selector]
  (let [container (query selector)
        slides (query-all container ".slide")
        index (or (query-initial-index slides) 1)
        {:keys [dispatch state] :as store} (create-store {:current index} reducers epics)]
    (dispatch {:type :count-slides :data (count slides)})
    (-> (ui-events container)
        (.takeUntil bus)
        (.onValue #(dispatch {:type (keyword %) :data nil})))
    store))
