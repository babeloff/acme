(ns flechar.model.person
  (:require
    [fulcro.client.mutations :as m]
    [taoensso.timbre :as log]))

(m/defmutation
  delete-person
  "Mutation: Delete the person with name from the list with list-name"
  [{:keys [list-id person-id]}]
  (action
    [{:keys [state]}]
    (log/info "deleting person " list-id " " person-id)
    (let [path  (cond
                  (= :friends list-id)
                  [:friends :person-list/people]

                  (= :enemies list-id)
                  [:enemies :person-list/people])
          _ (log/info "person " path " " list-id " " person-id)
          old-list (get-in @state path)
          new-list (vec (filter #(not= (:db/id %) person-id) old-list))]
      (swap! state assoc-in path new-list))))

