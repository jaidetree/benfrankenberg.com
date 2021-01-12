(ns src.com.benfrankenberg.tasks.lib.stream)

(def stream (js/require "highland"))

(def end (.-nil stream))

(defn end?
  "
  Takes any kind of value.
  Returns true if value is highland.nil signifying if value is a stream.
  "
  [x]
  (= x end))

(defn err?
  "
  Takes any kind of value.
  Returns true if value is not null. Useful checking the error param of
  of null-style callbacks.
  "
  [err]
  (not (nil? err)))

(defn merge
  "
  Takes a source stream and a list of other streams
  Returns a single stream of all streams merged together.

  Example:
  (-> (stream [1 2 3])
      (merge [(stream [4]) (stream [5]) (stream [6])])
      (.each println))
  ;; =>
  ;; 1
  ;; 2
  ;; 3
  ;; 4
  ;; 5
  ;; 6
  "
  [source sources]
  (-> (stream (clj->js (conj sources source)))
      (.merge)))

(defn take-until
  "
  Highland stream consumer function to take values from a stream until a valve
  stream emits a value signifying that the source stream should close.

  Takes a stream that emits a value when the source stream should be closed.
  Returns a consume function that is compatible with highland .consume
  operator.

  Example:
  (-> (interval 1000)
      (.consume (take-until (timeout 3000)))
      (.each println))
  ;; => 1000
  ;; => 2000
  ;; => 3000
  "
  [close-stream]
  (let [open? (atom true)
        ended? (atom false)]
    (fn [err x push next]
      (when open?
        (.pull close-stream
           (fn []
            (when-not ended?
              (reset! ended? true)
              (push nil end))))
        (reset! open? false))
      (cond (err? err)   (do (push err)
                             (next))
            (end? x)     (do (reset! ended? true)
                             (.destroy close-stream)
                             (push nil x))
            (not ended?) (do (push nil x)
                             (next))))))

(defn take-while
  "
  Highland stream consumer function to take values from a stream while the
  given predicate function returns true.

  Takes a predicate function to operate on each incoming value.
  Returns a consume function that is compatible with highland .consume
  operator.

  Example:
  (-> (range 1 100)
      (.consume (takeWhile #(< % 5)))
      (.each println))
  ;; =>
  ;; 0
  ;; 1
  ;; 2
  ;; 3
  ;; 4
  "
  [f]
  (let [ended (atom false)]
    (fn [err x push next]
      (cond (err? err)   (do (push err)
                             (reset! ended true))
            (end? x)     (do (reset! ended true)
                             (push nil x))
            (not @ended) (if (f x)
                           (do (push nil x)
                               (next))
                           (do (push nil x)
                               (push nil end)
                               (reset! ended true)))))))
