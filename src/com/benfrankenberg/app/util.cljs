(ns com.benfrankenberg.app.util)

(defn query
  [selector]
  (.querySelector js/document selector))

(defn query-all
  [selector]
  (.from js/Array (.querySelectorAll js/document selector)))
