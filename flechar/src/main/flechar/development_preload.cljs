(ns flechar.development-preload
  (:require [taoensso.timbre :as log]))

; Add code to this file that should run when the initial application is loaded in development mode.
; shadow-cljs already enables console print and plugs in devtools if they are on the classpath,

(js/console.log "Turning logging to :trace [there is no :all]  (in flechar.development-preload)")
(log/set-level! :trace)
