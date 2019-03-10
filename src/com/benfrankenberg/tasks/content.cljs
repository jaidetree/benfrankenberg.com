(ns src.com.benfrankenberg.tasks.content
  (:require [src.com.benfrankenberg.site.home :as home]))

(def Buffer (.-Buffer (js/require "buffer")))
(def gulp (js/require "gulp"))
(def hiccup (js/require "@thi.ng/hiccup"))
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
  (let [hiccup-array (clj->js hiccup-vec)
        html (.serialize hiccup hiccup-array)]
    html))

(defn static-page
  [{:keys [render output]}]
  (-> (.of stream output)
      (.map render)
      (.map hiccup->html)
      (.map (fn [html]
              (-> {:path (str cwd "/" output)
                   :cwd cwd
                   :contents (.from Buffer html)}
                  (clj->js)
                  (Vinyl.))))))

(.task gulp "content"
  (fn content
    []
    (-> [(static-page {:render home/render :output "index.html"})]
        (clj->js)
        (stream)
        (.merge)
        (.pipe (.dest gulp "dist")))))

