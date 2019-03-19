(ns com.benfrankenberg.app.scroll
  (:require
    [com.benfrankenberg.app.state :refer [bus]]))

(def bacon (.-Bacon js/window))

(defn scroll
  [el]
  (-> (.fromEvent bacon el "scroll")
      (.map #(.-scrollY el))
      (.takeUntil bus)
      (.onValue (.-log js/console))))
