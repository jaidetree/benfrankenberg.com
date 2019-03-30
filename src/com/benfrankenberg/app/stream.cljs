(ns com.benfrankenberg.app.stream
  (:require
   [bacon :as bacon :refer [End]]))

(defn from
  [source]
  (cond (sequential? source)       (from (clj->js source))
        (.isArray js/Array source) (.fromArray bacon source)
        :else                      (from [source])))

(defn of
  [source]
  (.once bacon source))
