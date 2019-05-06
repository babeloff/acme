(ns flechar.server-components.config
  (:require
    [mount.core :as core]
    [fulcro.server :as server]
    [taoensso.timbre :as log]))


(defn configure-logging! [config]
  (let [{:keys [taoensso.timbre/logging-config]} config]
    (fulcro.logging/set-logger!
      (fn [{:keys [file line]} level & args]
        (log/log! level :p [args] {:?ns-str file :?line line})))
    (log/info "Configuring Timbre with " logging-config)
    (log/merge-config! logging-config)))


(core/defstate config
               :start (let [{:keys [config] :or {config "config/dev.edn"}} (core/args)
                            configuration (server/load-config {:config-path config})]
                        (log/info "Loaded config" config)
                        (configure-logging! configuration)
                        configuration))

