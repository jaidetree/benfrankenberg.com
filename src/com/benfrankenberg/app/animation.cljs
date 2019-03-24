(ns com.benfrankenberg.app.animation
  (:refer-clojure :exclude [range])
  (:require [bacon :as bacon :refer [End]]))

(def time-started (atom 0))

(defn ease
  [x]
  (* x x))

(defn transition
  [duration ease-fn]
  (-> (.interval bacon (/ duration 100) 0)
      (.scan 0 inc)
      (.takeWhile #(<= % 100))
      (.map #(/ % 100))
      (.map ease-fn)))

(defn fade-in
  [opacity]
  (set! (-> js/document (.-body) (.-style) (.-opacity))
        opacity))

(defn go!
  []
  (-> (transition 1000 ease)
      (.onValue fade-in)))
