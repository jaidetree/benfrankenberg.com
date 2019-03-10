(ns src.com.benfrankenberg.tasks.build)

(def gulp (js/require "gulp"))

(.task gulp "build" (.parallel gulp "style" "content" "images"))
