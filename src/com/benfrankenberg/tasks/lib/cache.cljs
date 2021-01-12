(ns src.com.benfrankenberg.tasks.lib.cache)

(def crypto (js/require "crypto"))

(def cache (atom {}))

(defn clear
  "
  Clears the cache
  Updates the cache atom by resetting it to an empty hash-map.
  Returns the updated cache hash-map.

  Example:
  (cache/clear)
  ;; => {}
  "
  []
  (reset! cache {}))

(defn update
  "
  Updates the cache hash-map by associng a file path with the hash key of its
  contents.
  Takes a path string and a hash key string.
  Returns the updated cache hash-map.

  Example:
  (cache/update \"my-file.cljs\" \"aaaabbbbcccc\")
  ;; => {\"my-file.cljs\" \"aaaabbbbcccc\")
  "
  [path key]
  (swap! cache assoc path key))

(defn same?
  "
  Determine if the contents of a file has not changed.
  Takes a path string and a hash key string.
  Returns true if the path matches the given hash-key.

  Example:
  (cache/same? \"my-file.cljs\" \"aaaabbbbcccc\")
  ;; => true
  "
  [path key]
  (= (get @cache path) key))

(defn updated?
  "
  Determine if the contents of a file has changed.
  Takes a path string and a hash-key string.
  Returns true if the path does not match the given hash-key.

  Example:
  (cache/same? \"my-file.cljs\" \"bbbbccccdddd\")
  ;; => false
  "
  [path key]
  (not (same? path key)))

(defn file-updated?
  "
  Determine if a vinyl file hash key has changed since last read.
  Takes a vinyl file.
  Returns true if a file should skip the cache layer or if the hash key has
  changed since last read.

  Example:
  (cache/file-updated? (Vinyl. #js {:path \"my-file.cljs\"
                                    :hash \"eeeeffffgggg\"}))
  ;; => true
  "
  [file]
  (or (.-skip-cache file)
      (updated? (.-path file) (.-hash file))))

(defn hash-file
  "
  Generate a hash from a vinyl file's contents and store it on the vinyl obj.
  Takes a vinyl file.
  Mutates the vinyl file by adding a hash property on it.
  Returns the vinyl file.

  Example:
  (cache/hash-file (Vinyl. #js {:path \"my-file.cljs\"}))
  ;; => #Vinyl[object]
  "
  [file]
  (let [hash (.createHash crypto "md5")]
    (.update hash (.-contents file))
    (set! (.-hash file) (.digest hash "hex")))
  file)

(defn cache-file
  "
  Update the cache hash-map atom with the path of the file and the hash for
  the file's contents. Purely for side-effects.
  Takes a vinyl file object.
  Updates the hash-map atom.
  Returns the vinyl file.

  Example:
  (cache/cache-file (Vinyl. #js {:path \"my-file.cljs\"}))
  ;; => #Vinyl[object]
  "
  [file]
  (update (.-path file) (.-hash file))
  file)

(defn prevent-cache
  "
  Sets a skip-cache flag on a vinyl file to avoid being cached.
  Takes a vinyl file.
  Mutates the vinyl file by adding a \"skip-cache\" property to it.
  Returns the mutated vinyl file.

  Example:
  (cache/prevent-cache (Vinyl. #js {:path \"my-file.cljs\"}))
  ;; => #Vinyl[object]
  "
  [file]
  (set! (.-skip-cache file) true))
