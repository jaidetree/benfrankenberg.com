(ns src.com.benfrankenberg.site.tasks.develop)

(def gulp (js/require "gulp"))

(defn develop
  []
  (.watch gulp "./src/{com/benfrankenberg/site,img,scss}/**/*" (.series gulp "build")))

(set! (.-name develop) "develop")

(.task gulp "develop"
  (.parallel gulp develop "serve"))
