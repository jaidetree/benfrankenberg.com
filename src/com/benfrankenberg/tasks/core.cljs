(ns src.com.benfrankenberg.tasks.core
 (:require [src.com.benfrankenberg.tasks.content]
           [src.com.benfrankenberg.tasks.images]
           [src.com.benfrankenberg.tasks.serve]
           [src.com.benfrankenberg.tasks.style]
           [src.com.benfrankenberg.tasks.build]
           [src.com.benfrankenberg.tasks.develop]))

(def gulp (js/require "gulp"))

(.task gulp "hello" (fn
                      [done]
                      (println "hey there")
                      (done)))
