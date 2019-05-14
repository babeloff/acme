(ns flechar.model.person
  (:require
    [fulcro.client.mutations :as m]
    [taoensso.timbre :as log]))

(m/defmutation
  delete-person
  "Mutation: Delete the person with name from the list with list-name"
  [{:keys [list-name name]}]
  (action [{:keys [state]}]
    (let [path  (cond
                  (= "Friends" list-name)
                  [:friends :person-list/people]

                  (= "Enemies" list-name)
                  [:enemies :person-list/people]

                  :else
                  [:others  :person-list/people])
          old-list (get-in @state path)
          new-list (vec (filter #(not= (:person/name %) name) old-list))]
      (swap! state assoc-in path new-list))))

