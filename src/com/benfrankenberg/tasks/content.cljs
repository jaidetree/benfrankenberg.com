(ns src.com.benfrankenberg.tasks.content
  (:require
    [src.com.benfrankenberg.tasks.color :as c]
    [clojure.string :as s]
    [goog.object :as obj]))

(def Buffer (.-Buffer (js/require "buffer")))
(def cljs (js/require "clojurescript"))
(def gulp (js/require "gulp"))
(def hiccup (js/require "@thi.ng/hiccup"))
(def log (js/require "fancy-log"))
(def stream (js/require "@eccentric-j/highland"))
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

(defn hiccup->html
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

(defn load-render-fn
  [render-sym]
  (let [ns-name (namespace render-sym)
        f-name (name render-sym)
        node-name (str cwd "/" (s/replace ns-name #"\." "/"))]
    (.eval cljs (str "(ns importer
                        (:require [goog.object :as obj]))
                      (def module (js/require \"" node-name "\"))
                      (obj/get module \"" f-name "\")"))))

(defn static-page
  [{:keys [render output]}]
  (let [f (load-render-fn render)]
    (-> (.of stream output)
        (.map f)
        (.map hiccup->html)
        (.map (fn [html]
                (-> {:path (str cwd "/" output)
                     :cwd cwd
                     :contents (.from Buffer html)}
                    (clj->js)
                    (Vinyl.))))
        (.tap #(log-page "static" (.-relative %))))))

(.task gulp "content"
  (fn content
    []
    (-> [(static-page {:render 'src.com.benfrankenberg.site.home/render :output "index.html"})]
        (clj->js)
        (stream)
        (.merge)
        (.pipe (.dest gulp "dist")))))

