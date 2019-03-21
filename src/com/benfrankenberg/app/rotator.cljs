(ns com.benfrankenberg.app.rotator
  (:require
   [com.benfrankenberg.app.util :refer [query query-all]]
   [com.benfrankenberg.app.state :refer [bus create-store action?]]))

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

(defn query-initial-index
  [container selector]
  (some->> (query-all container selector)
           (map-indexed vector)
           (filter (fn [[_ el]] (.includes (.-className el) "active")))
           (first)
           (first)))

(def reducers {:rotated (fn [db action]
                          (assoc db :current (:data action)))})

(defn rotate-fx
  [actions]
  (-> actions
      (action? :next)
      (.map #(merge % {:type :rotate :data {:current (:data %)
                                            :to (inc (:data %))}}))))

(def epics [rotate-fx])

(defn ui-events
  [container]
  (-> #js [(.fromEvent bacon (query container ".next") "click")
           (.fromEvent bacon (query container ".prev") "click")]
      (->> (.concatAll bacon))
      (.map #(-> % (.-currentTarget) (.-value)))))

(defn rotator
  [selector]
  (let [container (query selector)
        index (or (query-initial-index container ".slide") 1)
        {:keys [dispatch state] :as store} (create-store {:current index} reducers epics)]
    (-> (ui-events container)
        (.zip state #(hash-map :type (keyword %1) :data (:current %2)))
        (.takeUntil bus)
        (.onValue #(dispatch %)))
    store))
