(ns com.benfrankenberg.app.core
  (:require
    [com.benfrankenberg.app.rotator :refer [rotator]]
    [com.benfrankenberg.app.scroll :refer [scroll-hero-opacity]]
    [com.benfrankenberg.app.state :refer [bus]]
    [com.benfrankenberg.app.util :refer [query]]))


(def bacon (.-Bacon js/window))

(defn init!
  []
  (scroll-hero-opacity)
  (rotator ".headshots"))

(defn reload!
  []
  (println "Reloading!")
  (.push bus (.now js/Date))
  (init!))
