(ns com.benfrankenberg.app.viewport)

(defn screen-width
  []
  (-> js/window
      (.-innerWidth)))

(defn mobile?
  []
  (<= (screen-width) 929))

(defn desktop?
  []
  (> (screen-width) 929))
