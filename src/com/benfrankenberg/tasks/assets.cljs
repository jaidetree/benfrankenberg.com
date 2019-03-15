(ns src.com.benfrankenberg.tasks.assets
  (:require
    [src.com.benfrankenberg.tasks.lib.color :as c]
    [src.com.benfrankenberg.tasks.lib.util :refer [base glob?]]))

(def gulp (js/require "gulp"))
(def log (js/require "fancy-log"))
(def stream (js/require "@eccentric-j/highland"))

(defn log-copy-file
  [file]
  (log (c/line (c/plugin "static")
               "Copied"
               (c/file (.-relative file)))))

(defn src-public
  [_]
  (-> (.src gulp #js ["src/public/**/*.*"] #js {:base (str (base) "/public")})
      (.pipe (stream))))

(defn copy-public-file
  [source]
  (-> source
      (.filter (glob? "src/public/**/*.*"))
      (.tap log-copy-file)
      (.tap #(set! (.-built %) true))))

(.task gulp "assets"
  (fn []
    (-> (src-public)
        (.tap log-copy-file)
        (.pipe (.dest gulp "./dist")))))
