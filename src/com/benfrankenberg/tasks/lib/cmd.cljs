(ns src.com.benfrankenberg.tasks.lib.cmd
  (:require
    [clojure.string :as s]
    [src.com.benfrankenberg.tasks.lib.color :as c]
    [src.com.benfrankenberg.tasks.lib.stream :as stream]
    [src.com.benfrankenberg.tasks.lib.util :refer [obj->clj]]))

(def cp (js/require "child_process"))
(def log (js/require "fancy-log"))
(def stream (js/require "highland"))

(defn tag-stream
  [type source]
  (-> source
      (.pipe (stream))
      (.map #(.toString % "utf-8"))
      (.split)
      (.compact)
      (.map #(hash-map :type type :data %))))

(defn stream-exit
  [type event-name source]
  (-> (stream event-name source #js ["code" "signal"])
      (.take 1)
      (.map obj->clj)
      (.map #(hash-map :type type :data %))))

(defn streamify
  [{:keys [child stdout stdin stderr]}]
  (-> (stream #js [(tag-stream :stdout stdout)
                   (tag-stream :stderr stderr)
                   (stream-exit :exit "exit" child)])
      (.merge)))

(defn exec
  [cmd-str]
  (log (c/line (c/plugin "scripts")
               "Executing"
               (c/file cmd-str)))
  (let [[cmd & args] (s/split cmd-str " ")
        child (.spawn cp cmd (clj->js args) #js {:stdio "pipe"})]
    (streamify {:child child
                :stdout (.-stdout child)
                :stdin (.-stdin child)
                :stderr (.-stderr child)})))

(defn on-stdout
  [f]
  (fn [update]
    (when (= (:type update) :stdout)
      (f (:data update))
      update)))

(defn on-stderr
  [f]
  (fn [update]
    (when (= (:type update) :stderr)
      (f (:data update))
      update)))

(defn on-exit
  [f]
  (fn [update]
    (when (= (:type update) :exit)
      (f (:data update))
      update)))

(defn until-exit
  [source]
  (-> source
      (.consume (stream/take-while #(not= (:type %) :exit)))))
