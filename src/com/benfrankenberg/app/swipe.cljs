(ns com.benfrankenberg.app.swipe
  (:require [bacon :as bacon]))

(defn do-when
  [source f]
  (if f (.doAction source f) source))

(defn cancel-events
  [gesture]
  (doseq [event (:events gesture)]
    (.preventDefault event)
    (.stopPropagation event)))

(defn bind-window-touch-move
  [binder listener]
  (binder "touchmove" listener #js {:passive false
                                    :capture true}))

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
  (> h 50))

(defn swipe?
  ([{:keys [v h ratio]}]
   (and (> h v)
        (< ratio 1)
        (>= h 100))))

(defn touch-end
  [{:keys [el on-end] :as opts}]
  (-> (.fromEvent bacon js/window "touchend")
      (.take 1)
      (.map opts)
      (do-when on-end)))

(defn touch-move
  [{:keys [el on-move]} start]
  (-> (.fromEvent bacon js/window "touchmove")
      (.doAction #(.preventDefault %))
      (.doAction #(.stopPropagation %))
      (.map #(events->gesture el [start %]))
      (.doAction #(println "move " (select-keys % [:h :v :ratio])))
      (.skipWhile #(not (swiping? %)))
      (.doAction #(println "swipe detected"))
      (.doAction cancel-events)
      (do-when on-move)))

(defn touch-start
  [{:keys [el on-start]}]
  (-> (.fromEvent bacon el "touchstart")
      (do-when on-start)))

(defn swipe
  [{:keys [el on-start on-move on-end] :as opts}]
  (-> (touch-start opts)
      (.flatMapFirst #(-> (touch-move opts %)
                          (.takeUntil (touch-end opts))
                          (.last)))
      (.filter swipe?)))
