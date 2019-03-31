(ns src.com.benfrankenberg.tasks.lib.util)

(def Minimatch (.-Minimatch (js/require "minimatch")))
(def absolute-glob (js/require "to-absolute-glob"))

(defn base
  []
  (str (.cwd js/process) "/src"))

(defn glob->pred
  [glob]
  (let [matcher (Minimatch. (absolute-glob glob))]
    (fn [path] (.match matcher path))))

(defn glob?
  ([patterns]
   (fn [file] (glob? patterns file)))
  ([patterns file]
   (if (string? patterns)
     (glob? [patterns] file)
     (let [path (.-path file)
           tests (map glob->pred patterns)]
      (every? (fn [test] (test path)) tests)))))

(defn obj->clj
  "Convert an object to clojure hash map. Not ideal but because objects may
  be created outside of this context it may fail a js->clj due to js/Object
  not matching the one from the context.
  Takes a JS object.
  Returns a Clojure hash-map."
  [o]
  (-> (.assign js/Object #js {} o)
      (js->clj :keywordize-keys true)))

(defn rename
  [file f]
  (set! (.-path file) (f (.-path file)))
  file)
