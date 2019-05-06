(ns flechar.ui.user-comp
  (:require
    [fulcro.client.dom :as dom]
    [fulcro.client.primitives :as prim]
    [flechar.model.user :as user]
    [flechar.ui.components :as comp]
    [taoensso.timbre :as log]))


(prim/defsc User [this {:user/keys    [name]
                        :address/keys [street city]}]
  {:query [:user/id :user/name :address/street :address/city]
   :ident [:user/id :user/id]}
  (dom/li :.ui.item
          (dom/div :.content)
          (str name (when street (str " of " street ", " city)))))

(def ui-user (prim/factory User {:keyfn :user/id}))




(prim/defsc UserButton [this {:user/keys [id]}]
  {:query [:user/id]
   :ident [:user/id]}
  (dom/button
    :.ui.icon.button
    {:onClick
     (fn []
       (let [id (str (random-uuid))]
         (log/info "Adding user")
         ;; NOTE: The lack of quoting works because we're using declare-mutation from incubator. see model.cljs
         ;; NOTE 2: This is a "mutation join".  The mutation is sent with a query, and on success it
         ;; returns the user.  This allows the server to tack on an address without us specifying it,
         ;; and also have the local database/ui update with it.
         (prim/transact! this [{(user/upsert-user {:user/id   id
                                                   :user/name (str "User " id)})
                                (prim/get-query User)}])))}
    (dom/i :.plus.icon)
    "Add User"))

(def ui-user-button (prim/factory UserButton))