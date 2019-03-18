(ns src.benfrankenberg.tasks.clojurescript
  (:require
    [clojure.string :as s]
    [src.com.benfrankenberg.tasks.lib.util :refer [base glob? rename]]))

(def Buffer (.-Buffer (js/require "buffer")))
(def cp (js/require "child_process"))
(def gulp (js/require "gulp"))
(def stream (js/require "@eccentric-j/highland"))
(def Vinyl (js/require "vinyl"))

(defn log-file
  [file]
  (println {:path (.-path file)
            :relative (.-relative file)}))

(.task gulp "build-cljs"
  (fn []
    (-> (.src gulp "src/com/benfrankenberg/ui/**/*.cljs" #js {:base (base)})
        (.pipe (stream))
        (.map compile-cljs-file)
        (.tap move-cljs-file)
        (.tap log-file)
        (.pipe (.dest gulp "./dist")))))
