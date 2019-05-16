(ns flechar.model.user
  (:require
    [fulcro.incubator.mutation-interface :as mi]
    [fulcro.client.mutations :as mu]))

(defn user-path
  "Normalized path to a user entity or field in Fulcro state-map"
  ([id field] [:db/id id field])
  ([id] [:db/id id]))

(defn insert-user*
  "Insert a user into the correct table of the Fulcro state-map database."
  [state-map {:keys [db/id] :as user}]
  (assoc-in state-map (user-path id) user))


;; IF you declare your mutations like this, then you can use them WITHOUT quoting in the UI!
(mi/declare-mutation upsert-user `upsert-user)
(mu/defmutation upsert-user
  "Client Mutation: Upsert a user (full-stack. see CLJ version for server-side)."
  [{:keys [db/id user/name] :as params}]
  (action [{:keys [state]}]
          (swap! state (fn [s]
                         (-> s
                             (insert-user* params)
                             (mu/integrate-ident* [:db/id id] :append [:root/all-users])))))
  (remote [env] true))


(mi/declare-mutation remove-user `remove-user)
(mu/defmutation remove-user
  "Client Mutation: Remove a user."
  [{:keys [db/id user/name] :as params}]
  (action [{:keys [state]}]
          (let [old-list (get-in @state :user/path)
                new-list (vec (filter #(not= (:person/name %) name) old-list))]
            (swap! state assoc-in :user/path new-list))))
