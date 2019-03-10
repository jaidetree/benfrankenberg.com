(ns src.com.benfrankenberg.tasks.style
  (:require [clojure.string :as s]
            [src.com.benfrankenberg.tasks.util :refer [rename]]))

(def gulp (js/require "gulp"))
(def stream (js/require "@eccentric-j/highland"))
(def sass (js/require "node-sass"))
(def Buffer (.-Buffer (js/require "buffer")))

(def render-css (.wrapCallback stream (.-render sass)))

(defn create-css-file
  [file compilation]
  (let [css-file (.clone file)
        css (-> compilation (.-css) (.toString))]
      (set! (.-contents css-file) (.from Buffer css))
      css-file))

(defn rename-scss-file
  [file-path]
  (s/replace file-path #"\.scss$" ".css"))

(defn scss->css
  [options]
  (fn [file]
    (-> options
        (js->clj)
        (merge {:file (.-path file)})
        (clj->js)
        (render-css)
        (.map #(create-css-file file %))
        (.map #(rename % rename-scss-file)))))

(.task gulp "style"
  (fn
   []
   (-> (.src gulp "src/scss/**/*.scss")
       (.pipe (stream))
       (.flatMap (scss->css {:outputStyle "compressed"}))
       (.pipe (.dest gulp "dist/css")))))
