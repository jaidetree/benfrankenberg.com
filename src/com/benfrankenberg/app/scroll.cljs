(ns com.benfrankenberg.app.scroll
  (:require
    [com.benfrankenberg.app.state :refer [bus]]
    [goog.object :as obj]))

(def bacon (.-Bacon js/window))

(defn query
  [selector]
  (.querySelector js/document selector))

(defn query-all
  [selector]
  (.from js/Array (.querySelectorAll js/document selector)))

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
      (.merge (.once bacon (.now js/Date)))
      (.map #(scroll-top ["html" "body"]))
      (.map #(scroll->percent % (top ".section.about")))
      (.takeUntil bus)
      (.onValue #(update-opacity [".background" ".hero"] %))))

(comment
  (-> (query-all "*")
      (.filter #(> (.-scrollTop %) 0))
      (.map #(str (.-tagName %) "." (.-className %)))))
