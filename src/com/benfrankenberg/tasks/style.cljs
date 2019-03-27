(ns src.com.benfrankenberg.tasks.style
  (:require [clojure.string :as s]
            [src.com.benfrankenberg.tasks.lib.cache :refer [prevent-cache]]
            [src.com.benfrankenberg.tasks.lib.color :as c]
            [src.com.benfrankenberg.tasks.lib.config :refer [env]]
            [src.com.benfrankenberg.tasks.lib.util :refer [base glob? rename]]))

(def autoprefixer (js/require "autoprefixer"))
(def gulp (js/require "gulp"))
(def log (js/require "fancy-log"))
(def cssnano (js/require "cssnano"))
(def postcss (js/require "postcss"))
(def stream (js/require "@eccentric-j/highland"))
(def sass (js/require "node-sass"))
(def Buffer (.-Buffer (js/require "buffer")))
(def Vinyl (js/require "vinyl"))

(def render-css (.wrapCallback stream (.-render sass)))

(defn log-css-file
  [filename]
  (log (c/line (c/plugin "style")
               "Compiled"
               (c/file filename))))

(defn scss-partial?
  [file]
  (let [basename (.-basename file)]
    (.startsWith basename "_")))

(defn src-scss
  [file]
  (-> (.src gulp #js ["src/scss/**/*.scss"
                      "!src/scss/**/_*.scss"]
                 #js {:base (base)})
      (stream)
      (.tap #(when (and file (scss-partial? file)) (prevent-cache %)))))

(defn create-css-file
  [file compilation]
  (let [css-file (.clone file)
        css (-> compilation (.-css) (.toString))]
      (set! (.-contents css-file) (.from Buffer css))
      css-file))

(defn rename-scss-file
  [file-path]
  (s/replace file-path #"scss" "css"))

(defn post-css
  [plugins]
  (let [postcss (postcss (clj->js plugins))]
    (fn [file]
      (-> postcss
          (.process (.-contents file) #js {:from nil})
          (stream)
          (.map (fn [result]
                  (let [css (.-css result)
                        file (.clone file)]
                    (set! (.-contents file) (.from Buffer css))
                    file)))))))

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
        (.flatMap (post-css (if (= env :development)
                              [autoprefixer]
                              [autoprefixer
                               cssnano])))
        (.tap #(log-css-file (.-relative %))))))

(defn scss->css
  [source]
  (-> source
      (.filter (glob? "src/scss/**/*.scss"))
      (.flatMap (scss {:outputStyle "expanded"}))))

(.task gulp "style"
  (fn
   []
   (-> (src-scss)
       (.flatMap (scss {:outputStyle "expanded"}))
       (.pipe (.dest gulp "./dist")))))
