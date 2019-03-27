(ns src.com.benfrankenberg.tasks.lib.config)

(def env (-> js/process
             (.-env)
             (.-NODE_ENV)
             (or "development")
             (keyword)))
