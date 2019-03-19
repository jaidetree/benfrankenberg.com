(ns com.benfrankenberg.app.core
  (:require
    [com.benfrankenberg.app.scroll :refer [scroll-hero-opacity]]
    [com.benfrankenberg.app.state :refer [bus]]
    [com.benfrankenberg.app.util :refer [query]]))


(def bacon (.-Bacon js/window))
(defn update-hero-height
  []
  (let [height (.-innerHeight js/window)
        els (map query [".hero" ".background"])]
    (doseq [el els]
      (set! (.-height (.-style el)) (str height "px")))))

(defn init!
  []
  (scroll-hero-opacity)
  (update-hero-height))

(defn reload!
  []
  (println "Reloading!")
  (.push bus (.now js/Date))
  (init!))
