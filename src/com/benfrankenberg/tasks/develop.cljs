(ns src.com.benfrankenberg.site.tasks.develop
  (:require
    [src.com.benfrankenberg.tasks.lib.cache :refer [cache-file file-updated? hash-file]]
    [src.com.benfrankenberg.tasks.lib.util :refer [base glob?]]
    [src.com.benfrankenberg.tasks.content :refer [hiccup->html]]
    [src.com.benfrankenberg.tasks.images :refer [optimize-images]]
    [src.com.benfrankenberg.tasks.style :refer [scss->css]]
    [src.com.benfrankenberg.tasks.serve :refer [browser-sync]]))

(def stream (js/require "@eccentric-j/highland"))
(def gulp (js/require "gulp"))
(def Vinyl (js/require "vinyl"))

(def sources #js ["./src/img/**/*"
                  "./src/scss/**/*.scss"
                  "./src/com/benfrankenberg/site/**/*.cljs"])

(defn watch-sources
  [globs]
  (let [watcher (.watch gulp globs)]
    (-> (stream "all" watcher #js ["event" "path"])
        (.map #(Vinyl. #js {:path (.-path %)
                            :base (base)
                            :event (.-event %)})))))

(defn read-source-files
  [globs]
  (fn [_]
    (-> (.src gulp globs #js {:base (base)})
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
            :hash (.-hash file)
            :built? (.-built file)}))

(defn built?
  [file]
  (= (.-built file) true))

(defn tag-build
  [file]
  (set! (.-built file) true)
  file)

(defn run-build
  [f source]
  (-> source
      (.fork)
      (.through f)
      (.tap tag-build)))

(defn build
  [source fs]
  (-> (map #(run-build % source) fs)
      (vec)
      (conj (.fork source))
      (clj->js)
      (stream)
      (.merge)))

(defn refresh
  [source]
  (-> source
      (.filter (glob? ["dist/img/**/*.{jpg,png}"]))
      (.debounce 100)
      (.tap #(.reload browser-sync))))

(defn dest
  [source out-dir]
  (-> source
      (.pipe (.dest gulp out-dir))
      (.pipe (stream))))

(.task gulp "watch"
  (fn []
    (-> (watch-sources sources)
        (.throttle 250)
        (start-with (Vinyl.))
        (.flatMap (read-source-files
                    (.concat sources "!./src/scss/**/_*.scss")))
        (.tap hash-file)
        (.filter file-updated?)
        (.tap cache-file)
        (build [scss->css
                hiccup->html
                optimize-images])
        (.filter built?)
        (dest "./dist")
        (refresh))))

(.task gulp "develop"
  (.parallel gulp "watch" "serve"))
