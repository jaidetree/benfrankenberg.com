(ns src.com.benfrankenberg.tasks.serve
  (:require [src.com.benfrankenberg.tasks.lib.color :as c]))

(def Buffer (.-Buffer (js/require "buffer")))
(def gulp (js/require "gulp"))
(def log (js/require "fancy-log"))
(def stream (js/require "@eccentric-j/highland"))
(def Vinyl (js/require "vinyl"))
(def browser-sync (.create (js/require "browser-sync")))

(.task gulp "serve"
  (fn
    []
    (.init browser-sync #js {:watch false
                             :open false
                             :ghostMode false
                             :server "./dist"})))
