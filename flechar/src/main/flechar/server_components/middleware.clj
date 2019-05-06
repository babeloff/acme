(ns flechar.server-components.middleware
  (:require
    [flechar.server-components.config :as fcf]
    [flechar.server-components.pathom :as fp]
    [mount.core :as core]
    [fulcro.server :as server]
    [ring.middleware.defaults :as rd]
    [ring.middleware.gzip :as rz]
    [ring.util.response :as resp]
    [hiccup.page :as hic]
    [taoensso.timbre :as log]))

(def ^:private not-found-handler
  (fn [req]
    {:status  404
     :headers {"Content-Type" "text/plain"}
     :body    "NOPE"}))


(defn wrap-api [handler uri]
  (fn [request]
    (if (= uri (:uri request))
      (server/handle-api-request
        fp/parser
        ;; this map is `env`. Put other defstate things in this map and they'll be
        ;; added to the resolver/mutation env.
        {:ring/request request}
        (:transit-params request))
      (handler request))))

;; ================================================================================
;; Dynamically generated HTML. We do this so we can safely embed the CSRF token
;; in a js var for use by the client.
;; ================================================================================
(defn index [csrf-token]
  (log/debug "Serving index.html")
  (hic/html5
    [:html {:lang "en"}
     [:head {:lang "en"}
      [:title "Flechar : Manipulation of Structure"]
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"}]
      [:link {:href "https://cdnjs.cloudflare.com/ajax/libs/semantic-ui/2.4.1/semantic.min.css"
              :rel  "stylesheet"}]
      [:link {:rel "shortcut icon" :href "data:image/x-icon;," :type "image/x-icon"}]
      [:script (str "var fulcro_network_csrf_token = '" csrf-token "';")]]
     [:body
      [:div#flechar]
      [:script {:src "js/main/main.js"}]
      [:script "flechar.client.init();"]]]))

;; ================================================================================
;; Workspaces can be accessed via shadow's http server on http://localhost:8023/workspaces.html
;; but that will not allow full-stack fulcro cards to talk to your server. This
;; page embeds the CSRF token, and is at `/wslive.html` on your server (i.e. port 3000).
;; ================================================================================
(defn wslive [csrf-token]
  (log/debug "Serving wslive.html")
  (hic/html5
    [:html {:lang "en"}
     [:head {:lang "en"}
      [:title "devcards"]
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"}]
      [:link {:href "https://cdnjs.cloudflare.com/ajax/libs/semantic-ui/2.4.1/semantic.min.css"
              :rel  "stylesheet"}]
      [:link {:rel "shortcut icon" :href "data:image/x-icon;," :type "image/x-icon"}]
      [:script (str "var fulcro_network_csrf_token = '" csrf-token "';")]]
     [:body
      [:div#flechar]
      [:script {:src "workspaces/js/main.js"}]]]))

(defn wrap-html-routes [ring-handler]
  (fn [{:keys [uri anti-forgery-token] :as req}]
    (cond
      (#{"/" "/index.html"} uri)
      (-> (resp/response (index anti-forgery-token))
        (resp/content-type "text/html"))

      ;; See note above on the `wslive` function.
      (#{"/wslive.html"} uri)
      (-> (resp/response (wslive anti-forgery-token))
        (resp/content-type "text/html"))

      :else
      (ring-handler req))))

(core/defstate
  middleware
  :start
  (let [defaults-config (:ring.middleware/defaults-config fcf/config)
        legal-origins (get fcf/config :legal-origins #{"localhost"})]
    (-> not-found-handler
        (wrap-api "/api")
        server/wrap-transit-params
        server/wrap-transit-response
        (wrap-html-routes)
        ;; If you want to set something like session store, you'd do it against
        ;; the defaults-config here (which comes from an EDN file, so it can't have
        ;; code initialized).
        ;; E.g. (rd/wrap-defaults (assoc-in defaults-config [:session :store] (my-store)))
        (rd/wrap-defaults defaults-config)
        rz/wrap-gzip)))
