(ns com.benfrankenberg.app.util)

(def bacon (.-Bacon js/window))

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

(defn with-latest-from
  [source secondary]
  (.flatMap
   source
   (fn [x]
     (-> (.once bacon x)
         (.zip (.take secondary 1) vector)))))
