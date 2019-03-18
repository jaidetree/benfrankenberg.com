(ns src.com.benfrankenberg.site.tasks.develop
  (:require
    [src.com.benfrankenberg.tasks.lib.cache :refer [cache-file file-updated? hash-file]]
    [src.com.benfrankenberg.tasks.lib.util :refer [base glob?]]
    [src.com.benfrankenberg.tasks.lib.stream :as stream]
    [src.com.benfrankenberg.tasks.assets :refer [copy-public-file src-public]]
    [src.com.benfrankenberg.tasks.scripts :refer [cljs->js]]
    [src.com.benfrankenberg.tasks.content :refer [hiccup->html src-hiccup]]
    [src.com.benfrankenberg.tasks.images :refer [optimize-images src-images]]
    [src.com.benfrankenberg.tasks.style :refer [scss->css src-scss]]
    [src.com.benfrankenberg.tasks.serve :refer [browser-sync]]))

(def gulp (js/require "gulp"))
(def stream (js/require "@eccentric-j/highland"))
(def Vinyl (js/require "vinyl"))

(def sources #js ["./src/img/**/*"
                  "./src/scss/**/*.scss"
                  "./src/com/benfrankenberg/site/**/*.cljs"])

(defn log-file
  [file]
  (println {:path (.-path file)
            :hash (.-hash file)
            :built? (.-built file)}))

(defn watch-sources
  [globs]
  (let [watcher (.watch gulp globs)]
    (-> (stream "all" watcher #js ["event" "path"])
        (.map #(Vinyl. #js {:path (.-path %)
                            :base (base)
                            :event (.-event %)})))))

(defn src
  [file src-fns]
  (-> ((apply juxt src-fns) file)
      (clj->js)
      (stream)
      (.series)))

(defn start-with
  [source-stream value]
  (let [value-stream (.of stream value)]
    (-> #js [value-stream source-stream]
        (stream)
        (.merge))))

(defn built?
  [file]
  (= (.-built file) true))

(defn tag-build
  [file]
  (set! (.-built file) true)
  file)

(defn run-build
  [source f]
  (-> source
      (.fork)
      (.through f)
      (.tap tag-build)))

(defn build
  [source fs]
  (-> (map #(run-build source %) fs)
      (vec)
      (conj (.fork source))
      (clj->js)
      (stream)
      (.merge)))

(defn refresh
  [source]
  (-> source
      (.debounce 100)
      (.tap #(.reload browser-sync))))

(defn dest
  [source out-dir]
  (-> source
      (.pipe (.dest gulp out-dir))
      (.pipe (stream))))

(defn report-error
  [err _]
  (.error js/console err))

(.task gulp "watch"
  (fn []
    (-> (watch-sources sources)
        (.throttle 250)
        (start-with (Vinyl. #js {:path "." :basename "root"}))
        (.flatMap #(src % [src-scss
                           src-hiccup
                           src-images
                           src-public]))
        (.tap hash-file)
        (.filter file-updated?)
        (.tap cache-file)
        (build [scss->css
                hiccup->html
                optimize-images
                copy-public-file])
        (stream/merge [(cljs->js)])
        (.filter built?)
        (.errors report-error)
        (dest "./dist")
        (refresh))))

(.task gulp "develop"
  (.parallel gulp "watch" "serve"))
