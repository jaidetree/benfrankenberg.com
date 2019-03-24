(ns com.benfrankenberg.app.stream
  (:require
   [bacon :as bacon :refer [End]]
   [com.benfrankenberg.app.raf :refer [kill-raf raf]]))

(defn next-frame
  [value]
  (-> (.fromBinder bacon
        (fn create [cb]
          (let [id (raf (fn []
                         (raf #(do (cb %)
                                   (cb (End.))))))]
            (fn cancel []
              (kill-raf id)))))
      (.map value)))

(defn delay-frame
  [source]
  (.sampledBy source (next-frame 1)))

(defn from
  [source]
  (cond (sequential? source)       (from (clj->js source))
        (.isArray js/Array source) (.fromArray bacon source)
        :else                      (from [source])))

(defn of
  [source]
  (.once bacon source))

(defn with-latest-from
  [source secondary]
  (.flatMap
   source
   (fn [x]
     (-> (.once bacon x)
         (.zip (.take secondary 1) vector)))))
