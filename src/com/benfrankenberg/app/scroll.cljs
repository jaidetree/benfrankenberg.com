(ns com.benfrankenberg.app.scroll
  (:require
    [goog.object :as obj]
    [com.benfrankenberg.app.state :refer [bus]]
    [com.benfrankenberg.app.stream :refer [next-frame]]
    [com.benfrankenberg.app.util :refer [query query-all]]))

(def bacon (.-Bacon js/window))

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

(defn scroll-top
  [selectors]
  (->> selectors
       (map query)
       (map #(.-scrollTop %))
       (apply max)))

(defn update-opacity
  [selectors opacity]
  (let [els (map query selectors)]
    (doseq [el els]
      (set! (-> el (.-style) (.-opacity)) opacity))))

(defn scroll-hero-opacity
  []
  (-> (.fromEvent bacon js/window "scroll")
      (.startWith (.now js/Date))
      (.map #(scroll-top ["html" "body"]))
      (.map #(scroll->percent % (top ".section.about")))
      (.takeUntil bus)
      (.onValue #(update-opacity [".background" ".hero"] %))))
