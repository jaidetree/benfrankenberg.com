(ns com.benfrankenberg.app.animation
  (:refer-clojure :exclude [range])
  (:require
   [bacon :as bacon :refer [End]]
   [com.benfrankenberg.app.raf :refer [cancel-frame-request request-frame]]))

(defn loop-frames
  [id cb]
  (letfn [(frame [] (reset! id (request-frame #(do (cb %) (frame)))))]
    (frame)
    id))

(defn frames
  []
  (.fromBinder bacon
    (fn create [cb]
      (let [id (atom -1)]
        (loop-frames id cb)
        (fn cancel []
          (cancel-frame-request @id))))))

(defn delay-frame
  [source]
  (-> source
      (.flatMap #(-> (frames)
                     (.take 3)
                     (.last)
                     (.map (constantly %))))))

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
  (-> (.once bacon 0)
      (.flatMap #(transition 1000 ease))
      (.doAction fade-in)
      (.onEnd identity)))
