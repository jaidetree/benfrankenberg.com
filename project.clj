(defproject com.benfrankenberg "0.1.0-SNAPSHOT"
  :description "Source for benfrankenberg.com"
  :url "https://benfrankenberg.com"
  :license {:name "Artistic-2.0"
            :url "https://opensource.org/licenses/Artistic-2.0"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.520"]
                 [thheller/shadow-cljs "2.8.18"]]
  :source-paths ["src"]
  :main ^:skip-aot lein-app.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
