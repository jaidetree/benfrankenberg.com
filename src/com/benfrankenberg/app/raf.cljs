(ns com.benfrankenberg.app.raf
  (:require [goog.object :as obj]))

(def bacon (.-Bacon js/window))
(def End (.-End bacon))

(defn any
  [choices]
  (->> choices
       (map #(obj/get js/window %))
       (filter identity)
       (first)))


(def raf (or (any ["requestAnimationFrame"
                   "webkitRequestAnimationFrame"
                   "mozRequestAnimationFrame"
                   "msRequestAnimationFrame"
                   "oRequestAnimationFrame"])
             (fn polyfill-request-animation-frame [cb]
               (js/setTimeout cb (/ 1000 60)))))

(def kill-raf (or (any ["cancelAnimationFrame"
                        "webkitCancelAnimationFrame"
                        "mozCancelAnimationFrame"
                        "msCancelAnimationFrame"
                        "oCancelAnimationFrame"])
                  js/clearTimeout))

(defn next-frame
  [value]
  (-> (.fromBinder bacon
        (fn create [cb]
          (let [id (raf (fn []
                         (raf #(do (cb %)
                                   (cb (End.))))))]
            (fn cancel []
              (kill-raf id)))))
      (.map value)))

(defn delay-frame
  [source]
  (.sampledBy source (next-frame 1)))
