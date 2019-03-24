(ns com.benfrankenberg.app.swipe
  (:require [bacon :as bacon]))

(defn do-when
  [source f]
  (if f (.doAction source f) source))

(defn normalize-touch
  [event]
  (as-> event $
        (.-changedTouches $)
        (.from js/Array $)
        (nth $ 0)
        (hash-map :x (.-pageX $)
                  :y (.-pageY $))))

(defn events->gesture
  [[start end]]
  (let [el (.-currentTarget end)
        width (.-clientWidth el)
        {start-y :y start-x :x} (normalize-touch start)
        {end-y   :y end-x   :x} (normalize-touch end)
        h (- end-x start-x)
        v (- end-y start-y)
        direction (if (> h 0) :prev :next)]
    {:events [start end]
     :el el
     :h (.abs js/Math h)
     :v (.abs js/Math v)
     :ratio (.abs js/Math (/ h v))
     :scale (max (min (/ (.abs js/Math h) 100) 1) 0)
     :direction direction
     :selector (str "." (name direction))}))

(defn swiping?
  [{:keys [v h]}]
  (and (> h v)
       (> h 0)))

(defn swipe?
  ([{:keys [v h ratio]}]
   (and (> h v)
        (< v 50)
        (> h 100))))

(defn cancel-events
  [gesture]
  (doseq [event (:events gesture)]
    (.preventDefault event)
    (.stopPropagation event)))

(defn prevent-scroll
  []
  (-> (.fromEvent bacon js/window
                  (fn [binder listener]
                    (binder "touchmove" listener #js {:passive false})))
      (.doAction #(.preventDefault %))
      (.filter false)))

(defn touch-end
  [{:keys [el on-end] :as opts}]
  (-> (.fromEvent bacon el "touchend")
      (.take 1)
      (.doAction ".preventDefault")
      (.map opts)
      (do-when on-end)))

(defn touch-move
  [{:keys [el on-move]} start]
  (-> (.fromEvent bacon el "touchmove")
      (.map #(events->gesture [start %]))
      (.skipWhile #(not (swiping? %)))
      (.merge (prevent-scroll))
      (.doAction cancel-events)
      (do-when on-move)))

(defn touch-start
  [{:keys [el on-start]}]
  (-> (.fromEvent bacon el "touchstart")
      (.doAction "preventDefault")
      (do-when on-start)))

(defn swipe
  [{:keys [el on-start on-move on-end] :as opts}]
  (-> (touch-start opts)
      (.flatMapFirst #(-> (touch-move opts %)
                          (.takeUntil (touch-end opts))
                          (.last)))
      (.filter swipe?)))
