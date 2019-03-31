(ns src.com.benfrankenberg.tasks.serve
  (:require [src.com.benfrankenberg.tasks.lib.color :as c]))

(def gulp (js/require "gulp"))
(def browser-sync (.create (js/require "browser-sync")))

(.task gulp "serve"
  (fn
    []
    (.init browser-sync #js {:watch false
                             :open false
                             :ghostMode false
                             :server "./dist"})))
