(ns com.benfrankenberg.app.state)

(defonce bacon (.-Bacon js/window))
(defonce Bus (.-Bus bacon))

;; Shared event handler
(defonce bus (Bus.))

(defn combine-reducers
  [reducer-map]
  (fn [db action]
    (let [action-type (:type action)]
      (if (contains? reducer-map action-type)
        ((get reducer-map action-type) db action)
        db))))

(defn combine-epics
  [epic-fns]
  (fn [actions state]
    (-> (.fromArray bacon (clj->js epic-fns))
        (.flatMap #(% actions state))
        (.takeUntil bus))))

(defn handle-fx
  [epic actions state dispatch]
  (-> (epic actions state)
      (.takeUntil bus)
      (.onValue dispatch)))

(defn create-store
  [initial reducer-map epics]
  (let [actions (Bus.)
        dispatch #(.push actions %)
        state (-> actions
                  (.doAction #(println "incoming action" %))
                  (.take 10)
                  (.scan initial (combine-reducers reducer-map))
                  (.takeUntil bus)
                  (.doAction #(println "resulting state" %)))]
    (.subscribe state identity)
    (handle-fx (combine-epics epics) actions state dispatch)
    (dispatch {:type :initialize :data {}})
    {:dispatch dispatch
     :actions actions
     :state state}))

(defn action?
  [actions expected-type]
  (-> actions
      (.filter #(= (:type %) expected-type))))


