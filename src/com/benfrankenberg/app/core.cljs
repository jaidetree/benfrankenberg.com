(ns com.benfrankenberg.app.core
  (:require
    [com.benfrankenberg.app.animation :refer [go!]]
    [com.benfrankenberg.app.rotator :refer [rotator]]
    [com.benfrankenberg.app.scroll :refer [scroll-hero-opacity]]
    [com.benfrankenberg.app.state :refer [bus]]
    [com.benfrankenberg.app.util :refer [query]]))


(def bacon (.-Bacon js/window))

(defn init!
  "
  Initialize our app.
  "
  []
  (scroll-hero-opacity)
  (rotator ".headshots")
  (go!))

(defn reload!
  "
  Restart the app.
  Only called during development.
  Should be factored out inproduction.
  "
  []
  (println "Reloading!")
  (.push bus (.now js/Date))
  (init!))
