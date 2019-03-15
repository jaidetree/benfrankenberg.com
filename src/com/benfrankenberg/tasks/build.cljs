(ns src.com.benfrankenberg.tasks.build)

(def gulp (js/require "gulp"))

(.task gulp "build" (.series gulp "style" "content" "images" "assets"))
