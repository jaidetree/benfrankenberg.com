(ns com.benfrankenberg.ui.scroll)

(def bacon (.-Bacon js/window))

(defn scroll
  [el]
  (-> (.fromEvent bacon el "scroll")
      (.map #(.-scrollY js/window))
      (.subscribe (.-log js/console))))
