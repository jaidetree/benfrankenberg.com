(ns com.benfrankenberg.app.core
  (:require
    [com.benfrankenberg.app.scroll :refer [scroll]]
    [com.benfrankenberg.app.state :refer [bus]]))


(def bacon (.-Bacon js/window))

(scroll js/window)

(defn reload!
  []
  (println "Reloading!")
  (.push bus (.now js/Date)))
