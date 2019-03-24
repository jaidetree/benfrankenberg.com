(ns com.benfrankenberg.app.state
  [:require
   [bacon :as bacon :refer [Bus]]
   [com.benfrankenberg.app.stream :as stream]])

;; Shared event handler
(defonce bus (Bus.))

(defn combine-reducers
  [reducer-map]
  (fn [db action]
    (let [action-type (:type action)]
      (if (contains? reducer-map action-type)
        ((get reducer-map action-type) db action)
        db))))

(defn combine-fx
  [fxs]
  (fn [actions state]
    (as-> fxs $
          (map #(% actions state) $)
          (clj->js $)
          (.mergeAll bacon $)
          (.takeUntil $ bus))))

(defn handle-fx
  [epic actions state dispatch]
  (-> (epic actions state)
      (.takeUntil bus)
      (.onValue dispatch)))

;; Public API
;; ---------------------------------------------------------------------------

(defn action?
  [actions & expected-types]
  (let [expected (set expected-types)]
    (-> actions
        (.filter #(expected (:type %))))))

(defn create-store
  [initial reducer-map fx]
  (let [actions (Bus.)
        dispatch #(.push actions %)
        state (-> actions
                  (.doAction #(println "incoming action" %))
                  (.scan initial (combine-reducers reducer-map))
                  (.takeUntil bus)
                  (.doAction #(println "resulting state" %)))]
    (handle-fx (combine-fx fx) actions state dispatch)
    (.subscribe state identity)
    (dispatch {:type :initialize :data {}})
    {:dispatch dispatch
     :actions actions
     :state state}))

(defn gen-action
  ([type]
   (fn [data] (gen-action type data)))
  ([type data]
   {:type type :data data}))
