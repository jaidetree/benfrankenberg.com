(ns src.com.benfrankenberg.tasks.scripts
  (:require
    [clojure.string :as s]
    [src.com.benfrankenberg.tasks.lib.cmd :as cmd]
    [src.com.benfrankenberg.tasks.lib.color :as c]
    [src.com.benfrankenberg.tasks.lib.stream :as stream]
    [src.com.benfrankenberg.tasks.lib.util :refer [obj->clj]]))

(def gulp (js/require "gulp"))
(def log (js/require "fancy-log"))

(defn log-file
  [file]
  (println {:path (.-path file)
            :relative (.-relative file)}))

(defn log-stdout
  [output]
  (log (c/line
         (c/plugin "scripts")
         output)))

(defn log-stderr
  [output]
  (log (c/line
         (c/plugin "scripts")
         (c/yellow output))))

(defn log-exit
  [{:keys [code]}]
  (log (c/line
         (c/plugin "scripts")
         "Exited:"
         (c/data code))))

(defn cljs->js
  [_]
  (-> (cmd/exec "npx shadow-cljs watch :app")
      (.tap (cmd/on-stdout log-stdout))
      (.tap (cmd/on-stderr log-stderr))
      (.tap (cmd/on-exit log-exit))
      (cmd/until-exit)
      (.reject (constantly true))))

(.task gulp "scripts"
  (fn []
    (-> (cmd/exec "npx shadow-cljs release :app")
        (.tap (cmd/on-stdout log-stdout))
        (.tap (cmd/on-stderr log-stderr))
        (.tap (cmd/on-exit log-exit))
        (cmd/until-exit))))

