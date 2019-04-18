(ns src.com.benfrankenberg.tasks.lib.config
  "
  Provides an env variable to refer to the build environment.
  Values will either be :development or the keyword value of NODE_ENV.
  ")

(def env (-> js/process
             (.-env)
             (.-NODE_ENV)
             (or "development")
             (keyword)))
