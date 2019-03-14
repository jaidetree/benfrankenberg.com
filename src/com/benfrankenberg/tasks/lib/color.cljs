(ns src.com.benfrankenberg.tasks.lib.color
  (:require [clojure.string :as s]))

(def color (js/require "ansi-colors"))

(defn black
 [& args]
 (.black color (apply str args)))

(defn blue
 [& args]
 (.blue color (apply str args)))

(defn cyan
 [& args]
 (.cyan color (apply str args)))

(defn gray
 [& args]
 (.gray color (apply str args)))

(defn green
 [& args]
 (.green color (apply str args)))

(defn magenta
 [& args]
 (.magenta color (apply str args)))

(defn red
 [& args]
 (.red color (apply str args)))

(defn yellow
 [& args]
 (.yellow color (apply str args)))

(defn white
 [& args]
 (.white color (apply str args)))

(defn plugin
  [name]
  (str "[" (green name) "]:"))

(defn file
  [filename]
  (magenta filename))

(defn data
  [input]
  (cyan input))

(defn line
  [& args]
  (s/join " " args))
