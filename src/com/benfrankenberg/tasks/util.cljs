(ns src.com.benfrankenberg.tasks.util)

(defn base
  []
  (str (.cwd js/process) "/src"))

(defn rename
  [file f]
  (set! (.-path file) (f (.-path file)))
  ; (set! (.-relative file) (f (.-relative file)))
  file)
