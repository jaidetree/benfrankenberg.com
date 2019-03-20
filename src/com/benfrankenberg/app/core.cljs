(ns com.benfrankenberg.app.core
  (:require
    [com.benfrankenberg.app.scroll :refer [scroll-hero-opacity]]
    [com.benfrankenberg.app.state :refer [bus]]
    [com.benfrankenberg.app.util :refer [query]]))


(def bacon (.-Bacon js/window))

(defn update-hero-height
  [height]
  (let [els (map query [".hero" ".background"])]
    (doseq [el els]
      (set! (.-height (.-style el)) (str height "px")))))

(defn screen-height
  []
  (let [outer (.-outerHeight js/window)
        inner (.-innerHeight js/window)
        screen (-> js/window (.-screen) (.-availHeight))]
    (if (zero? outer)
      screen
      inner)))

(defn init!
  []
  (scroll-hero-opacity)
  (-> (.fromEvent bacon js/window "resize")
      (.startWith (.now js/Date))
      (.map screen-height)
      (.log)
      (.takeUntil bus)
      (.onValue update-hero-height)))

(defn reload!
  []
  (println "Reloading!")
  (.push bus (.now js/Date))
  (init!))
