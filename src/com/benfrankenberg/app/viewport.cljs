(ns com.benfrankenberg.app.viewport)

(defn screen-width
  "
  Returns the width (Number) of the global window object in pixels.
  "
  []
  (-> js/window
      (.-innerWidth)))

(defn mobile?
  "
  Returns true if the window width is less than or equal to 929 pixels.
  "
  []
  (<= (screen-width) 929))

(defn desktop?
  "
  Returns true if the window width  is more than 929px
  "
  []
  (> (screen-width) 929))
