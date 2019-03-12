(ns src.com.benfrankenberg.site.tasks.develop
  (:require [src.com.benfrankenberg.tasks.util :refer [base]]))

(def stream (js/require "@eccentric-j/highland"))
(def gulp (js/require "gulp"))
(def Vinyl (js/require "vinyl"))

(.task gulp "auto-build"
  (fn develop
    []
    (.watch gulp "./src/{com/benfrankenberg/site,img,scss}/**/*"
            (.series gulp "build"))))

(.task gulp "develop"
  (.parallel gulp "auto-build" "serve"))

(defn stream-watch
  [watcher]
  (-> (stream "all" watcher #js ["event" "path"])
      (.map #(Vinyl. #js {:path (.-path %)
                          :base (base)
                          :event (.-event %)}))))

(.task gulp "watch"
  (fn []
    (-> (.watch gulp #js ["./src/img/**/*"
                          "./src/scss/**/*.scss"
                          "./src/com/benfrankenberg/site/**/*.cljs"]
                #js {:ignoreInitial false})
        (stream-watch)
        (.tap (.-log js/console)))))
