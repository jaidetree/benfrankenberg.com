(ns com.benfrankenberg.app.animation
  (:refer-clojure :exclude [range])
  (:require
   [bacon :as bacon :refer [End]]
   [goog.object :as obj]
   [com.benfrankenberg.app.state :refer [bus]]
   [com.benfrankenberg.app.stream :as stream]))

;; Request Animation Frame Primitives
;; ---------------------------------------------------------------------------

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

;; Animation Stream Primitives
;; ---------------------------------------------------------------------------

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
                     (.take 1)
                     (.map (constantly %))))))

(defn next-frame
  [data]
  (-> (stream/of data)
      (delay-frame)))

(defn ms-elapsed
  []
  (-> (.once bacon (.now js/Date))
      (.flatMap #(-> (frames)
                     (.map (.now js/Date))
                     (.map (fn [start]
                             (- (.now js/Date) start)))))))

;; Animation API
;; ---------------------------------------------------------------------------

(defn px-per-second
  [px]
  (-> (ms-elapsed)
      (.map (fn [ms] (/ (* px ms) 1000)))))

(defn duration
  [ms]
  (-> (ms-elapsed)
      (.map (fn [ems] (/ ems ms)))
      (.takeWhile #(< % 1))
      (.concat (next-frame 1))))

;; Easing Functions
;; ---------------------------------------------------------------------------

(defn ease
  [t]
  (if (< t 0.5)
    (* 4.0 t t t)
    (+ 1.0 (* 0.5 (.pow js/Math (- (* 2.0 t) 2.0) 3.0)))))

;; Effects
;; ---------------------------------------------------------------------------

(defn fade-in
  "Fade the body element in.
  Takes the opacity as a float percentage.
  Mutates the style of the HTML body tag."
  [opacity]
  (set! (-> js/document (.-body) (.-style) (.-opacity))
        opacity))

;; UI Effect Animations
;; ---------------------------------------------------------------------------

(defn go!
  "Run the animation"
  []
  (-> (duration 1000)
      (.map ease)
      (.takeUntil bus)
      (.doAction fade-in)
      (.log)))
  ; (-> (.once bacon 0)
  ;     (.flatMap #(transition 1000 ease))
  ;     (.doAction fade-in)
  ;     (.onEnd identity)))
