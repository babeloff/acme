(ns flechar.ui.user
  (:require
    ;; #?(:clj [fulcro.client.dom-server :as dom]
    ;;   :cljs [fulcro.client.dom :as dom]
    [fulcro.client.dom :as dom]
    [fulcro.client.primitives :as prim]
    [flechar.model.user :as user]
    [flechar.ui.svg :as svg]
    [taoensso.timbre :as log]))


(prim/defsc User
  [_ props]

  {:query         [:db/id :user/name :address/street :address/city]
   :ident         [:user/by-id :db/id]
   :initial-state (fn [{:keys [id name street city]}]
                    {:db/id          id
                     :user/name      name
                     :address/street street
                     :address/city   city})}

  (let [{:keys [db/id user/name address/street address/city]} props]
    (dom/li {:key id :id id :className "ui.item"}
            (dom/div {:className "content"}
                     (str name " " id (when street (str " of " street ", " city)))))))

(def ui-user (prim/factory User {:keyfn :db/id}))




(prim/defsc UserButton
  [this props]

  {:query         [:db/id :user-button/width :user-button/height :user-button/label]
   :ident         [:user-button/by-id :db/id]
   :initial-state (fn [{:keys [id w h label]}]
                    {:db/id              id
                     :user-button/width  w
                     :user-button/height h
                     :user-button/label  label})}

  (let [{:keys [db/id]} props]
    (dom/button
      {:key       id
       :id        id
       :className "ui.icon.button"
       :onClick
                  (fn []
                    (let [user-id (str (random-uuid))]
                      (log/info "Adding user " user-id " to " id)
                      (prim/transact! this [{(user/upsert-user {:db/id     user-id
                                                                :user/name (str "User " user-id)})
                                             (prim/get-query User)}])))}
      (dom/i {:className "plus.icon"} "Add User"))))

(def ui-user-button (prim/factory UserButton {:keyfn :user-button/id}))