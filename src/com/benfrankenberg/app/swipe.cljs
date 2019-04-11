(ns com.benfrankenberg.app.swipe
  (:require
   [bacon :as bacon]
   [com.benfrankenberg.app.stream :refer [bind-event do-when]]))

(defn normalize-touch
  "
  Takes a js touch event
  Returns a hash map with :x and :y coords relative to the page

  Example:
  (normalize-touch a-touch-move-event)
  ;; => {:x 300 :y 500}
  "
  [event]
  (as-> event $
        (.-changedTouches $)
        (.from js/Array $)
        (nth $ 0)
        (hash-map :x (.-pageX $)
                  :y (.-pageY $))))

(defn events->gesture
  "
  Takes an element and a vector of a touchstart and a touchmove or touchend
  event like [touchstart touchmove] or [touchstart touchend].

  Returns a hash-map to describe the gesture:
  :events vector     - Vector of touchstart and touch move\\end events.
  :el        Element - Element being swiped on
  :h         Number  - Amount of horizontal distance between touch events.
  :v         Number  - Amount of vertical distance between touch events.
  :ratio     Number  - Ratio of v / h
  :scale     Number  - h / 100 to return a 0–1 percent of full 100px swipe
  :direction keyword - :prev or :next depending on horizontal change
  :selector  str     - \".prev\" or \".next\"
  "
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
  "
  Predicate to determine if gesture is swiping.
  Takes a gesture hash-map.
  Returns true if horizontal distance is greater than 10px.
  "
  [{:keys [v h]}]
  (> h 10))

(defn swipe?
  "
  Predicate to determine if gesture was a full swipe
  Takes a gesture hash-map.
  Returns true if horizontal distance is greater than vertical. If the ratio
  between horz and vert is greater than 1 and the horizontal distance is more
  than 100px.
  "
  ([{:keys [v h ratio]}]
   (and (> h v)
        (< ratio 1)
        (>= h 100))))

(defn touch-end
  "
  Takes a hash-map of opts:
  :block-scroll? atom     - Holds a boolean value used to prevent user from
                            scrolling while swiping.
  :el            Element  - DOMElement to listen for touchend events
  :on-end        function - A side-effect function for responding to touchend
                            events from the element.

  Returns a Bacon stream of opts for one touch-end.
  "
  [{:keys [block-scroll? el on-end] :as opts}]
  (-> (.fromEvent bacon el (bind-event "touchend"))
      (.take 1)
      (.doAction #(reset! block-scroll? false))
      (.map opts)
      (do-when on-end)))

(defn touch-move
  "
  Takes a hash-map of opts:
  :block-scroll? atom     - Holds a boolean value used to prevent user from
                            scrolling while swiping.
  :el            Element  - DOMElement to listen for touchmove events
  :on-move       function - A side-effect function for responding to touchmove
                            events from the element.

  Returns a Bacon stream of touchmove events.
  "
  [{:keys [block-scroll? el on-move]} start]
  (-> (.fromEvent bacon el (bind-event "touchmove"))
      (.map #(events->gesture el [start %]))
      (.skipWhile #(not (swiping? %)))
      (.doAction #(reset! block-scroll? true))
      (do-when on-move)))

(defn touch-start
  "
  Takes a hash-map of opts:
  :el       Element  - DOMElement to listen for touchstart events
  :on-start function - Side-effect function for responding to touchstart
                       events from the element.

  Returns a Bacon stream of touchstart events.
  "
  [{:keys [el on-start]}]
  (-> (.fromEvent bacon el (bind-event "touchstart"))
      (do-when on-start)))

(defn global-touch-handler
  "
  Takes a hash-map of opts:
  :block-scroll? atom     - Holds a boolean value used to prevent user from
                            scrolling while swiping.

  If the boolean held in block-scroll? is truthy we prevent the user from
  scrolling the page while they are swiping.

  Returns an empty bacon stream. Side-effects only.
  "
  [{:keys [block-scroll?]}]
  (-> (.fromEvent bacon js/window (bind-event "touchmove"))
      (.filter #(= @block-scroll? true))
      (.doAction #(.preventDefault %))
      (.filter false)))

(defn swipe
  "
  General swipe detection handler.

  Takes a map of opts:
  Takes a hash-map of opts:
  :el       Element  - DOMElement to listen for touchmove events
  :on-start function - Side-effect function for responding to touchstart
                       events from the element.
  :on-move  function - Side-effect function for responding to touchmove
                       events from the element.
  :on-end   function - Side-effect function for responding to touchend
                       events from the element.

  Returns a Bacon stream of swipe gesture hash-maps.
  "
  [{:keys [el on-start on-move on-end] :as opts}]
  (let [block-scroll? (atom false)
        opts (assoc opts :block-scroll? block-scroll?)]
    (-> (touch-start opts)
        (.merge (global-touch-handler opts))
        (.flatMapLatest #(-> (touch-move opts %)
                             (.takeUntil (touch-end opts))
                             (.last)))
        (.filter swipe?))))
