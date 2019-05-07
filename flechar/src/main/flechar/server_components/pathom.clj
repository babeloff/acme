(ns flechar.server-components.pathom
  (:require
    [mount.core :as mc]
    [taoensso.timbre :as log]
    [com.wsscode.pathom.trace :as pt]
    [com.wsscode.pathom.connect :as pconn]
    [com.wsscode.pathom.core :as pcore]
    [com.wsscode.common.async-clj :as aclj]
    [clojure.core.async :as async]

    ;; Central registry
    [flechar.server-components.pathom-wrappers :as pw]
    [flechar.server-components.config :as fcf]

    ;; ALL namespaces that use pathom-wrappers MUST be included for auto-registration to work
    flechar.model.user))

(defn preprocess-parser-plugin
  "Helper to create a plugin that can view/modify the env/tx of a top-level request.

  f - (fn [{:keys [env tx]}] {:env new-env :tx new-tx})

  If the function returns no env or tx, then the parser will not be called (aborts the parse)"
  [f]
  {::pcore/wrap-parser
   (fn transform-parser-out-plugin-external [parser]
     (fn transform-parser-out-plugin-internal [env tx]
       (let [{:keys [env tx] :as req} (f {:env env :tx tx})]
         (if (and (map? env) (seq tx))
           (parser env tx)
           {}))))})

(defn log-requests [{:keys [env tx] :as req}]
  (log/debug "Pathom transaction:" (pr-str tx))
  req)

(mc/defstate parser
               :start
               (let [real-parser (pcore/parallel-parser
                                   {::pcore/mutate  pconn/mutate-async
                                    ::pcore/env     {::pcore/reader               [pcore/map-reader pconn/parallel-reader
                                                                                   pconn/open-ident-reader pcore/env-placeholder-reader]
                                                     ::pcore/placeholder-prefixes #{">"}}
                                    ::pcore/plugins [(pconn/connect-plugin {::pconn/register (vec (vals @pw/pathom-registry))})
                                                     (pcore/env-wrap-plugin (fn [env]
                                                                              ;; Here is where you can dynamically add things to the resolver/mutation
                                                                              ;; environment, like the server config, database connections, etc.
                                                                              (assoc env :config fcf/config)))
                                                     (preprocess-parser-plugin log-requests)
                                                     (pcore/post-process-parser-plugin pcore/elide-not-found)
                                                     pcore/request-cache-plugin
                                                     pcore/error-handler-plugin
                                                     pcore/trace-plugin]})
                     ;; NOTE: Add -Dtrace to the server JVM to enable Fulcro Inspect query performance traces to the network tab!
                     trace? (not (nil? (System/getProperty "trace")))]
                 (fn wrapped-parser [env tx]
                   (async/<!! (real-parser env (if trace?
                                                 (conj tx ::pt/trace)
                                                 tx))))))

