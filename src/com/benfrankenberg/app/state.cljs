(ns com.benfrankenberg.app.state
  [:require
   [bacon :as bacon :refer [Bus]]
   [com.benfrankenberg.app.stream :as stream]])

;; Shared event handler
(defonce bus (Bus.))

(defn combine-reducers
  "
  Takes a hash map of action type keywords to reducer functions. Calls the
  reducer function if the :type property of an action is found.
  Returns a function that takes the current state and the dispatched action.

  Example:
  (def reducer (combine-reducers
                {:up (fn [state {:keys [data]}] (+ state data))
                 :down (fn [state {:keys [data]}] (- state data))}
  (reducer 2 {:type :up :data 3}) ; => 5
  (reducer 5 {:type :down :data 3}) ; => 2
  "
  [reducer-map]
  (fn [db action]
    (let [action-type (:type action)]
      (if (contains? reducer-map action-type)
        ((get reducer-map action-type) db action)
        db))))

(defn combine-fx
  "
  Compose several fx stream functions into a single fx function.
  Takes a list of fx stream functions.
  Returns a handler function that takes a bacon stream of actions and a
  bacon stream of state.
  Handler function returns a stream of resulting actions to dispatch.

  Avoid recursive loops.

  Example:
  (defn start-fx
    [actions state]
    (-> actions
        (action? :start)
        (.map #(assoc % :type :ping))))

  (defn pong-fx
    [actions state]
    (-> actions
        (action? :ping)
        (.map #(assoc % :type :pong))))

  (def fx (combine-fx [start-fx pong-fx]))

  (-> (fx actions state)
      (.onValue dispatch))

  (dispatch {:type :start :data nil})
  "
  [fxs]
  (fn [actions state]
    (as-> fxs $
          (map #(% actions state) $)
          (clj->js $)
          (.mergeAll bacon $)
          (.takeUntil $ bus))))

(defn cause-fx
  "
  Takes the folowing params:
  fx      function - A fx handler function that takes state and actions stream.
  actions stream   - Bacon stream of dispatched actions
  state   stream   - Bacon property stream of latest state.

  Dispatches side-effect actions produced by the fx handler function.

  Returns a bacon stream subscription.
  "
  [fx actions state dispatch]
  (-> (fx actions state)
      (.takeUntil bus)
      (.onValue dispatch)))

;; Public API
;; ---------------------------------------------------------------------------

(defn action?
  "
  Create a dependent stream of actions containing only the expected types.

  Takes a bacon stream of actions and variadic expected type keywords.
  Returns a stream that only has actions of the expected type keywords.

  Example:
  (-> actions
      (action? :ping)
      (.log))
  (dispatch {:type :ping})
  (dispatch {:type :pong})
  ;; => {:type :ping}
  "
  [actions & expected-types]
  (let [expected (set expected-types)]
    (-> actions
        (.filter #(expected (:type %))))))

(defn create-store
  "
  Create a store map used to subscribe to state and dispatch changes to state.

  Takes an initial state value. Could be anything provided functions in the
  reducer-map supports it.
  Takes a hash map of action type keywords to reduce functions that return
  updated app state.
  Takes a list of side-effect functions that operate on a stream of actions
  and\\or a stream state.

  Returns a store hash-map:
  :dispatch function - Takes an action like {:type :ping :data \"hi\"}
  :actions  stream   - A bacon stream of dispatched actions.
  :state    stream   - A bacon stream of the latest application state.
  "
  [initial reducer-map fxs]
  (let [actions (Bus.)
        dispatch #(.push actions %)
        state (-> actions
                  (.scan initial (combine-reducers reducer-map))
                  (.takeUntil bus))]
    (cause-fx (combine-fx fxs) actions state dispatch)
    (.subscribe state identity)
    (dispatch {:type :initialize :data {}})
    {:dispatch dispatch
     :actions actions
     :state state}))

(defn gen-action
  "
  Creates an action object. Curried by way of multi-arg bodies.
  Takes a type keyword and data.
  Returns an action hash-map:
  :type keyword - Type of action to dispatch. Paired against reducer map.
  :data any     - Data of any type. Used by reducer or fx to update state.

  If only the type is provided a function is returned to take data before
  returning the action hash-map.

  Example:
  (gen-action :ping \"hi\")
  ;; => {:type :ping :data \"hi\"}
  ((gen-action :pong) true)
  ;; => {:type :pong :data \"blah\"}
  "
  ([type]
   (fn [data] (gen-action type data)))
  ([type data]
   {:type type :data data}))
