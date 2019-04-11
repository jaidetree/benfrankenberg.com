(ns com.benfrankenberg.app.stream
  (:require
   [bacon :as bacon :refer [End]]))

(defn bind-event
  "
  Create an event stream that can control the bubbling of events.
  For instance listening to touchmove events on the window and canceling them
  if we detect a horizontal swipe.
  Takes an event-name stream.
  Returns a function to bind an active event listener.

  Example:
  (-> (stream/on js/window (bind-event \"touchmove\"))
      (.doAction #(.preventDefault %))
      (.log))
  "
  [event-name]
  (fn [binder listener]
    (binder event-name listener #js {:passive false})))

(defn do-when
  "
  Perform a side-effect only if a side-effect function is callable.
  Takes a source bacon stream and a function or falsey value.
  Returns a bacon stream.

  Example:
  (-> (from [1 2 3])
      (do-when (.-log js/console)) ;; logs each value
      (.subscribe))
  (-> (from [1 2 3])
      (do-when (.-logger js/window)) ;; returns a valid stream but doesn't log
      (.subscribe))
  "
  [source f]
  (if (fn? f)
    (.doAction source f)
    source))

(defn from
  "
  Create a bacon stream from an iterable clojure object or js array.
  Takes a array, list, vector, or hash-map.
  Returns a Bacon stream.

  Example:
  (-> (stream/from [1 2 3])
      (.log))
  "
  [values]
  (cond (sequential? values)       (from (clj->js values))
        (.isArray js/Array values) (.fromArray bacon values)
        :else                      (from [values])))

(defn of
  "
  Creates a bacon stream that emits a single value.
  Takes a value to push to a stream.
  Returns a Bacon stream.

  Example:
  (-> (stream/of (.now js/Date))
      (.log))
  "
  [value]
  (.once bacon value))

(defn on
  "
  Stream a DOM event listener.
  Takes a subject and an event name string.
  Returns a bacon stream.

  Example:
  (-> (stream/on js/window \"scroll\")
      (.log))
  "
  [subject event-name]
  (.fromEvent bacon subject event-name))
