(ns com.benfrankenberg.app.intro
  (:require
    [com.benfrankenberg.app.animation :as animation]
    [com.benfrankenberg.app.dom :as dom]
   [com.benfrankenberg.app.state :refer [bus]]))

;; Effects
;; ---------------------------------------------------------------------------

(defn fade-in!
  "Fade the body element in.
  Takes the opacity as a float percentage.
  Mutates the style of the HTML body tag."
  [opacity]
  (let [el (.-body js/document)]
    (dom/style! el :opacity opacity)))

;; Animations
;; ---------------------------------------------------------------------------

(defn play-intro-animation
  "Fade in the body element over 1 second"
  []
  (-> (animation/duration 1000)
      (.map animation/cubic)
      (.takeUntil bus)
      (.onValue fade-in!)))
