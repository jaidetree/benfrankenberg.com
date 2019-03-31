(ns com.benfrankenberg.app.scroll
  (:require
    [goog.object :as obj]
    [bacon :as bacon]
    [com.benfrankenberg.app.state :refer [bus]]
    [com.benfrankenberg.app.util :refer [query query-all]]))

(defn pick-js
  "
  Create a hash-map consisting of selected keys from a JS obj
  Takes a JS object and a vector of key strings.
  Returns a hash map

  Example:
  (pick-js #js {:a 1 :b 2 :c 3} [\"a\" \"c\"])
  ;; => {:a 1 :c 3}
  "
  [o keys]
  (->> keys
       (map #(vector (keyword %) (obj/get o %)))
       (into {})))

(defn el->rect
  "
  Takes a DOMElement
  Returns a rectangle hash-map.

  Example:
  (el->rect (query \".header\"))
  ;; => {:left 100 :top 10 :right 100 :bottom 580}
  "
  [el]
  (-> el
      (.getBoundingClientRect)
      (pick-js ["left" "top" "right" "bottom"])))

(defn top
  "
  Get the top position of element relative to browser scroll position.
  Takes a selector string
  Returns a number

  Example:
  (top \".header\")
  ;; => 10
  "
  [selector]
  (-> selector
      (query)
      (el->rect)
      (:top)))

(defn scroll->percent
  "
  Calculate the percentage of a scrollY against a total height of a container.
  Takes a scroll-y position Number and total height Number.
  Returns Number

  Example:
  (scroll->percent 42 300)
  ;; => 1
  "
  [scroll-y height]
  (->> scroll-y
       (+ height)
       (/ scroll-y)
       (min 1)
       (max 0)))

(defn scroll-top
  "
  Get the maximum scroll top from a list of selectors. Useful for browser
  compatability.
  Takes a collection of selector strings.
  Returns a number.

  Example:
  (scroll-top [\"html\" \"body\"])
  ;; => 80
  "
  [selectors]
  (->> selectors
       (map query)
       (map #(.-scrollTop %))
       (apply max)))

(defn update-opacity
  "
  Mutate DOM element's opacity style property.
  Takes a collection of selector strings and opacity number between 0 and 1.

  Example:
  (update-opacity [\"hero\" \"background\"])
  "
  [selectors opacity]
  (let [els (map query selectors)]
    (doseq [el els]
      (set! (-> el (.-style) (.-opacity)) opacity))))

(defn scroll-hero-opacity
  "
  Creates a bacon stream to respond to browser scroll events to fade out the
  hero and fixed background as the user scrolls to the about section.

  Returns a bacon stream subscription.
  "
  []
  (-> (.fromEvent bacon js/window "scroll")
      (.startWith (.now js/Date))
      (.map #(scroll-top ["html" "body"]))
      (.map #(scroll->percent % (top ".section.about")))
      (.map #(- 1 %))
      (.takeUntil bus)
      (.onValue #(update-opacity [".background" ".hero"] %))))
