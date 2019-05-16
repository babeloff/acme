(ns flechar.ui.person
  (:require
    ;; #?(:clj [fulcro.client.dom-server :as dom]
    ;;   :cljs [fulcro.client.dom :as dom]
    [fulcro.client.dom :as dom]
    [fulcro.client.primitives :as prim]
    [flechar.model.person :as person]
    [taoensso.timbre :as log]))



(prim/defsc Person
  [_ props computed]

  {:query [:db/id :person/name :person/age]
   :ident [:person/by-id :db/id]
   :initial-state (fn [{:keys [id name age]}]
                    {:db/id id :person/name name :person/age age})}
  ;; render
  (let [{:keys [db/id person/name person/age]} props
        {:keys [onDelete]} computed]
    (dom/li {:key id}
      (dom/h5 (str name " (age: " age ")")
              (dom/button {:onClick #(onDelete id)} " Y ")))))

(def ui-person (prim/factory Person {:keyfn :person/name}))


(def initial-friends
  [(prim/get-initial-state Person {:id 1 :name "Sally" :age 32})
   (prim/get-initial-state Person {:id 2 :name "Joe" :age 22})])

(def initial-enemies
  [(prim/get-initial-state Person {:id 3 :name "Fred" :age 11})
   (prim/get-initial-state Person {:id 4 :name "Bobby" :age 55})])


(prim/defsc PersonList
  [this props]

  {:query [:db/id :person-list/label {:person-list/people (prim/get-query Person)}]
   :ident [:person-list/by-id :db/id]
   :initial-state
          (fn [{:keys [id label]}]
              {:db/id id
               :person-list/label  label
               :person-list/people
               (cond
                 (= label "Friends") initial-friends
                 (= label "Enemies") initial-enemies)})}

  (let [db-id (:db/id props)
        label (:person-list/label props)
        people-list (:person-list/people props)
        delete-person (fn [person-id]
                        (prim/transact! this
                                        `[(person/delete-person {:list-id ~db-id,
                                                                 :person-id  ~person-id})]))]
    (dom/div {:key "person-div"}
      (dom/h4 label)
      (dom/ul {:key "person-ul"}
        (map (fn [p] (ui-person (prim/computed p {:onDelete delete-person}))) people-list)))))

(def ui-person-list (prim/factory PersonList))