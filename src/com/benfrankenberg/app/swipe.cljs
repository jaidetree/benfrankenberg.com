(ns com.benfrankenberg.app.swipe
  (:require [bacon :as bacon]))

(defn do-when
  [source f]
  (if f (.doAction source f) source))

(defn bind-event
  [event-name]
  (fn [binder listener]
    (binder event-name listener #js {:passive false})))

(defn normalize-touch
  [event]
  (as-> event $
        (.-changedTouches $)
        (.from js/Array $)
        (nth $ 0)
        (hash-map :x (.-pageX $)
                  :y (.-pageY $))))

(defn events->gesture
  [el [start end]]
  (let [width (.-clientWidth el)
        {start-y :y start-x :x} (normalize-touch start)
        {end-y   :y end-x   :x} (normalize-touch end)
        h (- end-x start-x)
        v (- end-y start-y)
        direction (if (> h 0) :prev :next)]
    {:events [start end]
     :el el
     :h (.abs js/Math h)
     :v (.abs js/Math v)
     :ratio (.abs js/Math (/ v h))
     :scale (max (min (/ (.abs js/Math h) 100) 1) 0)
     :direction direction
     :selector (str "." (name direction))}))

(defn swiping?
  [{:keys [v h]}]
  (> h 10))

(defn swipe?
  ([{:keys [v h ratio]}]
   (and (> h v)
        (< ratio 1)
        (>= h 100))))

(defn touch-end
  [{:keys [block-scroll? el on-end] :as opts}]
  (-> (.fromEvent bacon el (bind-event "touchend"))
      (.take 1)
      (.doAction #(reset! block-scroll? false))
      (.map opts)
      (do-when on-end)))

(defn touch-move
  [{:keys [block-scroll? el on-move]} start]
  (-> (.fromEvent bacon el (bind-event "touchmove"))
      (.map #(events->gesture el [start %]))
      (.skipWhile #(not (swiping? %)))
      (.doAction #(reset! block-scroll? true))
      (do-when on-move)))

(defn touch-start
  [{:keys [el on-start]}]
  (-> (.fromEvent bacon el (bind-event "touchstart"))
      (do-when on-start)))

(defn global-touch-handler
  [{:keys [block-scroll?]}]
  (-> (.fromEvent bacon js/window (bind-event "touchmove"))
      (.filter #(= @block-scroll? true))
      (.doAction #(.preventDefault %))
      (.filter false)))

(defn swipe
  [{:keys [el on-start on-move on-end] :as opts}]
  (let [block-scroll? (atom false)
        opts (assoc opts :block-scroll? block-scroll?)]
    (-> (touch-start opts)
        (.merge (global-touch-handler opts))
        (.flatMapLatest #(-> (touch-move opts %)
                             (.takeUntil (touch-end opts))
                             (.last)))
        (.filter swipe?))))
