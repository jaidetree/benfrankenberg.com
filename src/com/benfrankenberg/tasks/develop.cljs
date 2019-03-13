(ns src.com.benfrankenberg.site.tasks.develop
  (:require [src.com.benfrankenberg.tasks.util :refer [base]]
            [src.com.benfrankenberg.tasks.cache :refer [cache-file file-updated? hash-file]]))

(def stream (js/require "@eccentric-j/highland"))
(def gulp (js/require "gulp"))
(def Vinyl (js/require "vinyl"))

(def sources #js ["./src/img/**/*"
                  "./src/scss/**/*.scss"
                  "./src/com/benfrankenberg/site/**/*.cljs"])

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

(defn stream-sources
  [_]
  (let [sources (.concat sources "!./src/scss/**/_*.scss")]
    (-> (.src gulp sources #js {:base (base)})
        (stream))))

(defn start-with
  [source-stream value]
  (let [value-stream (.of stream value)]
    (-> #js [value-stream source-stream]
        (stream)
        (.merge))))

(defn log-file
  [file]
  (println {:path (.-path file)
            :hash (.-hash file)}))

(.task gulp "watch"
  (fn []
    (-> (.watch gulp sources)
        (stream-watch)
        (.throttle 250)
        (start-with (Vinyl.))
        (.flatMap stream-sources)
        (.tap hash-file)
        (.filter file-updated?)
        (.tap cache-file)
        (.tap log-file))))
