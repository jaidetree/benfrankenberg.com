(ns src.com.benfrankenberg.tasks.lib.color
  (:require [clojure.string :as s]))

(def color (js/require "ansi-colors"))

(defn black
  "
  Color the given string args black.
  Takes a list of string args.
  Returns a color formatted string.
  "
  [& args]
  (.black color (apply str args)))

(defn blue
  "
  Color the given string args blue
  Takes a list of string args.
  Returns a color formatted string.
  "
  [& args]
  (.blue color (apply str args)))

(defn cyan
  "
  Color the given string args cyan
  Takes a list of string args.
  Returns a color formatted string.
  "
  [& args]
  (.cyan color (apply str args)))

(defn gray
  "
  Color the given string args gray.
  Takes a list of string args.
  Returns a color formatted string.
  "
  [& args]
  (.gray color (apply str args)))

(defn green
  "
  Color the given string args green.
  Takes a list of string args.
  Returns a color formatted string.
  "
  [& args]
  (.green color (apply str args)))

(defn magenta
 "
 Color the given string args magenta.
 Takes a list of string args.
 Returns a color formatted string.
 "
 [& args]
 (.magenta color (apply str args)))

(defn red
  "
  Color the given string args red.
  Takes a list of string args.
  Returns a color formatted string.
  "
  [& args]
  (.red color (apply str args)))

(defn yellow
  "
  Color the given string args yellow.
  Takes a list of string args.
  Returns a color formatted string.
  "
  [& args]
  (.yellow color (apply str args)))

(defn white
  "
  Color the given string args white.
  Takes a list of string args.
  Returns a color formatted string.
  "
  [& args]
  (.white color (apply str args)))

(defn plugin
  "
  Style the name of a tooling plugin.
  Takes the name of the plugin
  Returns a decorated, color formatted string.
  "
  [name]
  (str "[" (green name) "]:"))

(defn file
  "
  Style a file name for consistent formatting using magenta.
  Takes a filename string.
  Returns a color formatted string.
  "
  [filename]
  (magenta filename))

(defn data
  "
  Style data for consistent formatting using cyan.
  Returns a color formatted string.
  "
  [input]
  (cyan input))

(defn line
  "
  Takes a list of string args
  Joins them with a space to form a formatted line using ANSI color escapes.

  Example:
  (line (cyan \"Greetings\")
        (magenta \"hello world\"))
  ;; => \"<cyan>Greetings</cyan> <magenta>hello world</magenta>\"
  "
  [& args]
  (s/join " " args))
