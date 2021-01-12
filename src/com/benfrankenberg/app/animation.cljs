(ns com.benfrankenberg.app.animation
  (:refer-clojure :exclude [range])
  (:require
   [bacon :as bacon :refer [End]]
   [goog.object :as obj]
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
  "
  A recursive function to keep requesting animation frames.

  Takes an id atom to store the the frame request resource id and a callback
  function to emit the time of the requested animation frame.

  If the id atom returns nil the loop stops.

  Returns the id atom.
  "
  [id cb]
  (when (some? @id)
    (reset! id
            (request-frame #(do (cb %)
                                (loop-frames id cb)))))
  id)

(defn frames
  "
  Returns a bacon stream of animation frames.

  Example:
  (-> (frames)
      (.log))
  "
  []
  (.fromBinder bacon
    (fn create [cb]
      (let [id (atom -1)]
        (loop-frames id cb)
        (fn cancel []
          (cancel-frame-request @id)
          (reset! id nil))))))

(defn delay-frame
  "
  Delay a bacon stream by 1 animation frame.
  Takes a bacon stream source.
  Returns a new bacon stream.

  Example:
  (-> (.once bacon \"hello world\")
      (delay-frame)
      (.log))
  "
  [source]
  (-> source
      (.flatMap #(-> (frames)
                     (.take 1)
                     (.map (constantly %))))))

(defn next-frame
  "
  Delay a value by a single animation frame.

  Takes data to emit.
  Returns a bacon stream that emits the data after 1 animation frame.
  Will not start until at least one subscriber is added.

  Example:
  (-> (next-frame \"hello\")
      (.log))
  "
  [data]
  (-> (stream/of data)
      (delay-frame)))

(defn ms-elapsed
  "
  Create a bacon stream that emits the time elapsed since creation against
  each animation frame.

  Will not start until at least one subscribe is listening.
  Returns a bacon stream of ms elapsed since animation starts.

  Example:
  (-> (ms-elapsed)
      (.takeWhile #(< % 1000))
      (.log))
  ;; Logs elapsed ms for every animation frame within a second.
  ;; Fast computers may output a lot of values while slower computers will
  ;; emit less.
  "
  []
  (-> (.once bacon (.now js/Date))
      (.flatMap #(-> (frames)
                     (.map (.now js/Date))
                     (.map (fn [start]
                             (- (.now js/Date) start)))))))

;; Animation API
;; ---------------------------------------------------------------------------

(defn px-per-second
  "
  Updates by an amount of pixels for ms. Useful for looping animations such
  as spinners.

  Takes a number of pixels to adjust each second.
  Returns a bacon stream of how many pixels an object should change.

  Example:
  (-> (ms-elapsed 25)
      (.takeWhile #(< % 3000))
      (.log))
  "
  [px]
  (-> (ms-elapsed)
      (.map (fn [ms] (/ (* px ms) 1000)))))

(defn duration
  "
  Creates a bacon stream that emits an animation percentage for each time
  within a time window of ms.

  Takes a number of ms for the entire duration.
  Returns a bacon stream that emits a percentage on each animation frame 0–1.

  Example:
  (-> (duration 2000)
      (.log))
  "
  [ms]
  (-> (ms-elapsed)
      (.map (fn [ems] (/ ems ms)))
      (.takeWhile #(< % 1))
      (.concat (next-frame 1))))

;; Easing Functions
;; ---------------------------------------------------------------------------

(defn ease
  "
  Takes a time percentage 0–1 and returns an animation % decimal.

  Example:
  (-> (duration 1000)
      (.map ease)
      (.log))
  "
  [t]
  (if (< t 0.5)
    (* 4.0 t t t)
    (+ 1.0 (* 0.5 (.pow js/Math (- (* 2.0 t) 2.0) 3.0)))))

(defn cubic
  "
  Takes a time percentage 0–1 and returns an animation % decimal.

  Example:
  (-> (duration 1000)
      (.map cubic)
      (.log))
  "
  [t]
  (- (* 3 (* t t))
     (* 2 (* t t t))))

(defn quad
  "
  Takes a time percentage 0–1 and returns an animation % decimal.

  Example:
  (-> (duration 1000)
      (.map quad)
      (.log))
  "
  [t]
  (let [x (/ t 0.5)]
    (if (< x 1)
      (* 0.5 x x)
      (let [x (dec x)]
        (* -0.5 (- (* x (- x 2)) 1))))))

(defn quart
  "
  Takes a time percentage 0–1 and returns an animation % decimal.

  Example:
  (-> (duration 1000)
      (.map quart)
      (.log))
  "
  [t]
  (if (< t 0.5)
    (* 8.0 (.pow js/Math t 4))
    (+ (* -8.0 (.pow js/Math (- t 1.0) 4)) 1.0)))

(defn sine
  "
  Takes a time percentage 0–1 and returns an animation % decimal.

  Example:
  (-> (duration 1000)
      (.map sine)
      (.log))
  "
  [t]
  (* -0.5 (- (.cos js/Math (* (.-PI js/Math) t)) 1)))
