(ns src.com.benfrankenberg.tasks.core
 (:require [src.com.benfrankenberg.tasks.content]
           [src.com.benfrankenberg.tasks.style]
           [src.com.benfrankenberg.tasks.build]))

(def gulp (js/require "gulp"))

(.task gulp "hello" (fn
                      [done]
                      (println "hey there")
                      (done)))
