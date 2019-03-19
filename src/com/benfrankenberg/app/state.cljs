(ns com.benfrankenberg.app.state)

(defonce bacon (.-Bacon js/window))
(defonce bus (new (.-Bus bacon)))
