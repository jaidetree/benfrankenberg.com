(ns com.benfrankenberg.app.util)

(defn query
  ([selector]
   (query js/document selector))
  ([container selector]
   (.querySelector container selector)))

(defn query-all
  ([selector]
   (query-all js/document selector))
  ([container selector]
   (js->clj (.from js/Array (.querySelectorAll container selector)))))
