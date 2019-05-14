(ns flechar.ui.person-comp
  (:require
    ;; #?(:clj [fulcro.client.dom-server :as dom]
    ;;   :cljs [fulcro.client.dom :as dom]
    [fulcro.client.dom :as dom]
    [fulcro.client.primitives :as prim]
    [flechar.model.person :as person]
    [taoensso.timbre :as log]))


(prim/defsc Person [this {:keys [person/name person/age]} {:keys [onDelete]}]
  {:query         [:person/name :person/age]
   :initial-state (fn [{:keys [name age] :as params}] {:person/name name :person/age age})}
  (dom/li
    (dom/h5 (str name " (age: " age ")")
            (dom/button {:onClick #(onDelete name)} "X"))))

(def ui-person (prim/factory Person {:keyfn :person/name}))



(prim/defsc PersonList [this {:keys [person-list/label person-list/people]}] ;
  {:query [:person-list/label {:person-list/people (prim/get-query Person)}]
   :initial-state
          (fn [{:keys [label]}]
            {:person-list/label  label
             :person-list/people (cond
                                   (= label "Friends")
                                   [(prim/get-initial-state Person {:name "Sally" :age 32})
                                    (prim/get-initial-state Person {:name "Joe" :age 22})]

                                   (= label "Enemies")
                                   [(prim/get-initial-state Person {:name "Fred" :age 11})
                                    (prim/get-initial-state Person {:name "Bobby" :age 55})]

                                   :else
                                   [(prim/get-initial-state Person {:name "Veroniqua" :age 14})
                                    (prim/get-initial-state Person {:name "Betty" :age 15})])})}

  (let [delete-person (fn [name]
                        (prim/transact! this
                                        `[(person/delete-person {:list-name ~label, :name ~name})]))]
    (dom/div
      (dom/h4 label)
      (dom/ul
        (map (fn [p] (ui-person (prim/computed p {:onDelete delete-person}))) people)))))

(def ui-person-list (prim/factory PersonList))