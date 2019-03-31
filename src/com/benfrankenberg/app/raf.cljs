(ns com.benfrankenberg.app.raf
  (:require
   [goog.object :as obj]))

(defn any
  "
  Takes a list of property names.
  Returns the first property that exists on the window.
  "
  [choices]
  (->> choices
       (map #(obj/get js/window %))
       (filter identity)
       (first)))

(def request-frame
  (or (any ["requestAnimationFrame"
            "webkitRequestAnimationFrame"
            "mozRequestAnimationFrame"
            "msRequestAnimationFrame"
            "oRequestAnimationFrame"])
      (fn polyfill-request-animation-frame [cb]
        (js/setTimeout cb (/ 1000 60)))))

(def cancel-frame-request
  (or (any ["cancelAnimationFrame"
            "webkitCancelAnimationFrame"
            "mozCancelAnimationFrame"
            "msCancelAnimationFrame"
            "oCancelAnimationFrame"])
      js/clearTimeout))
