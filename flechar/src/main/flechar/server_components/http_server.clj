(ns flechar.server-components.http-server
  (:require
    [flechar.server-components.config :as cf]
    [flechar.server-components.middleware :as fsc]
    [mount.core :as core]
    [clojure.pprint :as pp]
    [org.httpkit.server :as http-kit]
    [taoensso.timbre :as log]))

(core/defstate
  http-server
  :start
  (let [cfg (::http-kit/config cf/config)]
    (log/info "Starting HTTP Server with config " (with-out-str (pp/pprint cfg)))
    (http-kit/run-server fsc/middleware cfg))
  :stop (http-server))
