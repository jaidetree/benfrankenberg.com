(ns src.com.benfrankenberg.tasks.content
  (:require
    [clojure.string :as s]
    [goog.object :as obj]
    [src.com.benfrankenberg.tasks.lib.color :as c]
    [src.com.benfrankenberg.tasks.lib.util :refer [base glob?]]))

(def Buffer (.-Buffer (js/require "buffer")))
(def cljs (js/require "clojurescript"))
(def gulp (js/require "gulp"))
(def hiccup (js/require "@thi.ng/hiccup"))
(def log (js/require "fancy-log"))
(def reload (js/require "../../../src/js/hacks/reload.js"))
(def stream (js/require "highland"))
(def Vinyl (js/require "vinyl"))

(def cwd (.cwd js/process))

(defn to-hiccup
  "Used as toHiccup method on cljs vectors and lists.
  Takes no arguments but uses js-this.
  Returns a js array for thi.ng umbrella hiccup compatability."
  []
  (->> (js-this)
       (clj->js)))

;; Add a toHiccup method to cljs vectors and sequences
(doseq [o [[] ()]]
  (-> (.getPrototypeOf js/Object o)
      (.-toHiccup)
      (set! to-hiccup)))

(defn serialize-hiccup
  [hiccup-vec]
  (let [html (.serialize hiccup hiccup-vec)]
    html))

(defn log-page
  [file-type filename]
  (log (c/line (c/plugin "content")
               "Generated"
               (c/data file-type)
               "page"
               (c/file filename))))

(defn src-hiccup
  [_]
  (-> (.src gulp "src/com/benfrankenberg/site/**/*.cljs" #js {:base (base)})
      (stream)))

(defn load-render-fn
  [render-sym]
  (let [ns-name (namespace render-sym)
        f-name (name render-sym)
        node-module (str cwd "/" (s/replace ns-name #"\." "/"))
        module (reload node-module)]
    (obj/get module f-name)))

(defn static-page
  [{:keys [render output]}]
  (-> (.of stream render)
      (.map load-render-fn)
      (.map apply)
      (.map serialize-hiccup)
      (.map (fn [html]
              (-> {:path (str cwd "/" output)
                    :cwd cwd
                    :contents (.from Buffer html)}
                  (clj->js)
                  (Vinyl.))))
      (.tap #(log-page "static" (.-relative %)))))

(defn build-content
  []
  (-> #js [(static-page {:render 'src.com.benfrankenberg.site.home/render
                         :output "index.html"})]
      (stream)
      (.merge)))

(defn hiccup->html
  [source]
  (-> source
      (.filter (glob? "src/com/benfrankenberg/site/**/*.cljs"))
      (.flatMap build-content)))

(.task gulp "content"
  (fn content
    []
    (-> (build-content)
        (.pipe (.dest gulp "dist")))))
