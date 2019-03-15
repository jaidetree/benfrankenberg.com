(ns src.com.benfrankenberg.tasks.images
  (:require [src.com.benfrankenberg.tasks.lib.color :as c]
            [src.com.benfrankenberg.tasks.lib.util :refer [base glob?]]))

(def gulp (js/require "gulp"))
(def imagemin (js/require "imagemin"))
(def jpeg (js/require "imagemin-jpegtran"))
(def log (js/require "fancy-log"))
(def png (js/require "imagemin-pngquant"))
(def stream (js/require "@eccentric-j/highland"))

(def gb (* 1024 1024 1024))
(def mb (* 1024 1024))
(def kb 1024)

(defn format-size
  [size]
  (cond (>= size gb) (str (.toFixed (/ size gb) 1) "gb")
        (>= size mb) (str (.toFixed (/ size mb) 1) "mb")
        (>= size kb) (str (.toFixed (/ size kb) 1) "kb")
        :else        (str size "b")))

(defn log-compression
  [filename total updated]
  (let [delta (- total updated)
        percent (.toFixed (* (/ delta total) 100) 2)]
    (log (c/line (c/plugin "images")
                 "Compressed"
                 (c/file filename)
                 (c/data (format-size total))
                 "➞"
                 (c/data (format-size updated))
                 (c/green "(" percent "%)")))))

(defn src-images
  [_]
  (-> (.src gulp "src/img/**/*.{jpg,png}" #js {:base (base)})
      (.pipe (stream))))

(defn optimize-img
  [options]
  (fn [file]
    (let [plugins #js [(jpeg :jpg options)
                       (png)]
          filename (.-relative file)
          total (.. file -contents -length)]
      (-> (.buffer imagemin
                  (.-contents file)
                  #js {:plugins plugins})
          (stream)
          (.tap #(log-compression filename total (.-length %)))
          (.map (fn [contents]
                  (.clone file #js {"contents" contents})))))))

(defn optimize-images
  [source]
  (-> source
      (.filter (glob? "src/img/**/*.{jpg,png}"))
      (.flatMap (optimize-img {:jpg {:quality "65-80"}}))))

(.task gulp "images"
 (fn []
  (-> (.src gulp "src/img/**/*.{jpg,png}" #js {:base (base)})
      (.pipe (stream))
      (.flatMap (optimize-img {:jpg {:quality "65-80"}}))
      (.pipe (.dest gulp "./dist")))))

