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
  "
  Uses a type keyword to transform each line of output from a stream into
  hash-maps of a given type.
  Takes a type keyword and a highland source stream.
  Returns a highland stream that emits hash maps like:
  {:type :stdout :data \"Enumerating objects: 45, done.\"}
  "
  [type source]
  (-> source
      (.pipe (stream))
      (.map #(.toString % "utf-8"))
      (.split)
      (.compact)
      (.map #(hash-map :type type :data %))))

(defn stream-exit
  "
  Takes a child-process object.
  Returns a stream that emits a hash-map when the child-process exits:
  {:type :exit :data {:code 0 :signal nil}

  If a signal was used the :code will be nil, if the program exits on its own
  :code will be used and :signal will be nil.
  "
  [emitter]
  (-> (stream "exit" emitter #js ["code" "signal"])
      (.take 1)
      (.map obj->clj)
      (.map #(hash-map :type :exit :data %))))

(defn ->stream
  "
  Takes a hash-map like the following:
  :child  child-process - Node Child Process to listen to events on
  :stdout Readable      - Readable node stdout stream from the child process
  :stdin  Writable      - Writable node stdin stream from the child process
  :stderr Readable      - Readable node stderr stream from the child process

  Returns a stream that emits hash-maps:
  :type keyword      - Type of output may be :stdout :stderr or :exit.
  :data str|hash-map - Line of output if stdout or stderr, hash-map if exit.
  "
  [{:keys [child stdout stdin stderr]}]
  (-> (stream #js [(tag-stream :stdout stdout)
                   (tag-stream :stderr stderr)
                   (stream-exit child)])
      (.merge)))

(defn exec
  "
  Execute a unix command in a subprocess and stream a series of hash-maps
  representing all types of output.

  Takes a command string to run.
  Returns a stream of hash-maps similar to the following:
  {:type :stdout :data \"Enumerating objects: 45, done\"}
  {:type :stdout :data \"Couning objects: 100% (45/45), done.\"}
  {:type :stderr :data \"No directory matching 'baobob' found.\"}
  {:type :exit   :data {:exit 0 :signal nil}}

  Example:
  (-> (cmd/exec \"git status\")
      (.each println))
  "
  [cmd-str]
  (log (c/line (c/plugin "scripts")
               "Executing"
               (c/file cmd-str)))
  (let [[cmd & args] (s/split cmd-str " ")
        child (.spawn cp cmd (clj->js args) #js {:stdio "pipe"})]
    (->stream {:child child
               :stdout (.-stdout child)
               :stdin (.-stdin child)
               :stderr (.-stderr child)})))

(defn on-stdout
  "
  A function to apply to a cmd stream to operate on stdout.
  Takes a function to operate on stdout content.
  Returns a function that takes an update action emitted by a cmd stream.

  Example:
  (-> (cmd/exec \"git status\")
      (.tap (cmd/on-stdout println))
      (.each identity))
  "
  [f]
  (fn [update]
    (when (= (:type update) :stdout)
      (f (:data update))
      update)))

(defn on-stderr
  "
  A function to apply to a cmd stream to operate on stderr.
  Takes a function to operate on stderr content.
  Returns a function that takes an update action emitted by a cmd stream.

  Example:
  (-> (cmd/exec \"git checkout blah\")
      (.tap (cmd/on-stderr println))
      (.each identity))
  "
  [f]
  (fn [update]
    (when (= (:type update) :stderr)
      (f (:data update))
      update)))

(defn on-exit
  "
  A function to apply to a cmd stream to operate on the exit.
  Takes a function to operate on exit status.
  Returns a function that takes an update action emitted by a cmd stream.

  Example:
  (-> (cmd/exec \"git checkout blah\")
      (.tap (cmd/on-exit println))
      (.each identity))
  "
  [f]
  (fn [update]
    (when (= (:type update) :exit)
      (f (:data update))
      update)))

(defn until-exit
  "
  Accepts all command output updates until exit signal is emitted.
  Takes a highland stream of command output actions.
  Returns a highland stream that ends when an :exit action occurs.

  Example:
  (-> (cmd/exec \"git checkout blah\")
      (cmd/until-exit)
      (.each println))
  "
  [source]
  (-> source
      (.consume (stream/take-while #(not= (:type %) :exit)))))
