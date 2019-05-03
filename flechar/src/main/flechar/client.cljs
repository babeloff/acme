(ns flechar.client
  (:require [fulcro.client :as fc]
            [flechar.ui.root :as root]
            [fulcro.client.network :as net]
            [fulcro.client.data-fetch :as df]))

(defonce single-page-app (atom nil))

(defn mount []
  (reset! single-page-app (fc/mount @single-page-app root/Root "app")))

(defn start []
  (mount))

(def secured-request-middleware
  ;; The CSRF token is embedded via server_components/html.clj
  (->
    (net/wrap-csrf-token (or js/fulcro_network_csrf_token "TOKEN-NOT-IN-HTML!"))
    (net/wrap-fulcro-request)))

(defn ^:export init []
  (reset! single-page-app (fc/make-fulcro-client
                {:client-did-mount (fn [flechar]
                                     (df/load flechar :all-users root/User))
                 ;; This ensures your client can talk to a CSRF-protected server.
                 ;; See middleware.clj to see how the token is embedded into the HTML
                 :networking {:remote (net/fulcro-http-remote
                                        {:url                "/api"
                                         :request-middleware secured-request-middleware})}}))
  (start))
