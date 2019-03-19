(ns com.benfrankenberg.app.core
  (:require
    [com.benfrankenberg.app.scroll :refer [scroll-hero-opacity]]
    [com.benfrankenberg.app.state :refer [bus]]))


(def bacon (.-Bacon js/window))
(defn init!
  []
  (scroll-hero-opacity))

(defn reload!
  []
  (println "Reloading!")
  (.push bus (.now js/Date))
  (init!))
