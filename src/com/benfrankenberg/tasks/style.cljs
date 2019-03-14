(ns src.com.benfrankenberg.tasks.style
  (:require [clojure.string :as s]
            [src.com.benfrankenberg.tasks.lib.color :as c]
            [src.com.benfrankenberg.tasks.lib.util :refer [base glob? rename]]))

(def gulp (js/require "gulp"))
(def log (js/require "fancy-log"))
(def stream (js/require "@eccentric-j/highland"))
(def sass (js/require "node-sass"))
(def Buffer (.-Buffer (js/require "buffer")))


(def render-css (.wrapCallback stream (.-render sass)))

(defn log-css-file
  [filename]
  (log (c/line (c/plugin "style")
               "Compiled"
               (c/file filename))))

(defn create-css-file
  [file compilation]
  (let [css-file (.clone file)
        css (-> compilation (.-css) (.toString))]
      (set! (.-contents css-file) (.from Buffer css))
      css-file))

(defn rename-scss-file
  [file-path]
  (s/replace file-path #"scss" "css"))

(defn scss
  [options]
  (fn [file]
    (-> options
        (js->clj)
        (merge {:file (.-path file)})
        (clj->js)
        (render-css)
        (.map #(create-css-file file %))
        (.map #(rename % rename-scss-file))
        (.tap #(log-css-file (.-relative %))))))

(defn scss->css
  [source]
  (-> source
      (.filter (glob? "src/scss/**/*.scss"))
      (.flatMap (scss {:outputStyle "compressed"}))))

(.task gulp "style"
  (fn
   []
   (-> (.src gulp "src/scss/**/*.scss" #js {:base (base)})
       (.pipe (stream))
       (.filter #(not (.startsWith (.-basename %) "_")))
       (.flatMap (scss {:outputStyle "compressed"}))
       (.pipe (.dest gulp "./dist")))))
