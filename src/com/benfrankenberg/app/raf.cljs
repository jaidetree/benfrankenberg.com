(ns com.benfrankenberg.app.raf
  (:require
   [goog.object :as obj]))

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
