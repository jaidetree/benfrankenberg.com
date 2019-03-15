(ns src.com.benfrankenberg.tasks.lib.cache)

(def crypto (js/require "crypto"))

(def cache (atom {}))

(defn clear
  []
  (reset! cache {}))

(defn update
  [path key]
  (swap! cache assoc path key))

(defn same?
  [path key]
  (= (get @cache path) key))

(defn updated?
  [path key]
  (not (same? path key)))

(defn file-updated?
  [file]
  (or (.-skip-cache file)
      (updated? (.-path file) (.-hash file))))

(defn hash-file
  [file]
  (let [hash (.createHash crypto "md5")]
    (.update hash (.-contents file))
    (set! (.-hash file) (.digest hash "hex")))
  file)

(defn cache-file
  [file]
  (update (.-path file) (.-hash file))
  file)

(defn prevent-cache
  [file]
  (set! (.-skip-cache file) true))
