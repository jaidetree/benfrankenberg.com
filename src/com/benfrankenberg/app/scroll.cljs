(ns com.benfrankenberg.app.scroll
  (:require
    [com.benfrankenberg.app.state :refer [bus]]
    [goog.object :as obj]))

(def bacon (.-Bacon js/window))

(defn query
  [selector]
  (.querySelector js/document selector))

(defn pick
  [o keys]
  (->> keys
       (map #(vector (keyword %) (obj/get o %)))
       (into {})))

(defn el->rect
  [el]
  (-> el
      (.getBoundingClientRect)
      (pick ["left" "top" "right" "bottom"])))

(defn top
  [selector]
  (-> selector
      (query)
      (el->rect)
      (:top)))

(defn scroll->percent
  [scroll-y target-y]
  (->> scroll-y
       (+ target-y)
       (/ scroll-y)
       (min 1)
       (max 0)
       (- 1)))

(defn update-hero-opacity
  [opacity]
  (doseq [el [(query ".background")
              (query ".hero")]]
   (set! (-> el (.-style) (.-opacity)) opacity)))

(defn scroll
  [el]
  (-> (.fromEvent bacon el "scroll")
      (.map #(.-scrollY el))
      (.map #(scroll->percent % (top ".section.about")))
      (.takeUntil bus)
      (.log "opacity")
      (.onValue update-hero-opacity)))
