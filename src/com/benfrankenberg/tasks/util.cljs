(ns src.com.benfrankenberg.tasks.util)

(defn rename
  [file f]
  (set! (.-path file) (f (.-path file)))
  ; (set! (.-relative file) (f (.-relative file)))
  file)
