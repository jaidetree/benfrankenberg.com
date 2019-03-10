(defproject com.benfrankenberg "0.1.0-SNAPSHOT"
  :description "Source for benfrankenberg.com"
  :url "https://benfrankenberg.com"
  :license {:name "Artistic-2.0"
            :url "https://opensource.org/licenses/Artistic-2.0"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [hiccups "0.3.0"]]
  :main ^:skip-aot lein-app.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
