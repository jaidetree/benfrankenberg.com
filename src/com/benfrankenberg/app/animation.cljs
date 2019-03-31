(ns com.benfrankenberg.app.animation
  (:refer-clojure :exclude [range])
  (:require
   [bacon :as bacon :refer [End]]
   [com.benfrankenberg.app.state :refer [bus]]
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
                     (.take 1)
                     (.map (constantly %))))))

(def time-started (atom 0))

(defn ease
  [x]
  (* x x))

(defn transition
  "Takes a duration in ms and an easing function.
  Returns a bacon stream that emits a percentage so that it reaches 1 at the
  end of the duration."
  [duration ease-fn]
  (-> (.interval bacon (/ duration 100) 0)
      (.scan 0 inc)
      (.takeWhile #(<= % 100))
      (.map #(/ % 100))
      (.map ease-fn)))

(defn fade-in
  "Fade the body element in.
  Takes the opacity as a float percentage.
  Mutates the style of the HTML body tag."
  [opacity]
  (set! (-> js/document (.-body) (.-style) (.-opacity))
        opacity))

(defn ms-elapsed
  []
  (-> (.once bacon (.now js/Date))
      (.flatMap (fn [_]
                  (-> (.interval bacon 0 (.now js/Date))
                      (.combine (frames) (fn [x _] x))
                      (.map (fn [start]
                              (- (.now js/Date) start))))))))

(defn go!
  "Run the animation"
  []
  (-> (ms-elapsed)
      (.takeUntil bus)
      (.take 25)
      (.log)))
  ; (-> (.once bacon 0)
  ;     (.flatMap #(transition 1000 ease))
  ;     (.doAction fade-in)
  ;     (.onEnd identity)))
