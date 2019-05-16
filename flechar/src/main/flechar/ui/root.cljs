(ns flechar.ui.root
  (:require
    ;; #?(:clj  [fulcro.client.dom-server :as dom]
    ;;    :cljs [fulcro.client.dom :as dom])
    [fulcro.client.dom :as dom]
    [fulcro.client.primitives :as prim]
    [flechar.ui.person-comp :as pc]
    [flechar.ui.svg_comp :as svg]
    [flechar.ui.user-comp :as uuc]
    [flechar.ui.victory :as victor]
    [taoensso.timbre :as log]))


(prim/defsc Root [_ props]

  {:query         [:ui/react-key
                   {:root/all-users (prim/get-query uuc/User)}
                   {:root/friends (prim/get-query pc/PersonList)}
                   {:root/enemies (prim/get-query pc/PersonList)}
                   :label :x-step :plot-data]
   :initial-state (fn [_]
                    {:ui/react-key   "initial"
                     :root/all-users (prim/get-initial-state uuc/User
                                                             {:id "my-self"
                                                              :name "Fred"
                                                              :street "Music Row"
                                                              :city "Nashville"})
                     :root/friends (prim/get-initial-state pc/PersonList {:id :friends :label "Friends"})
                     :root/enemies (prim/get-initial-state pc/PersonList {:id :enemies :label "Enemies"})

                     :label     "Yearly Value"
                     :x-step    2
                     :plot-data [{:year 1983 :value 100}
                                 {:year 1984 :value 100}
                                 {:year 1985 :value 90}
                                 {:year 1986 :value 89}
                                 {:year 1987 :value 88}
                                 {:year 1988 :value 85}
                                 {:year 1989 :value 83}
                                 {:year 1990 :value 80}
                                 {:year 1991 :value 70}
                                 {:year 1992 :value 80}
                                 {:year 1993 :value 90}
                                 {:year 1994 :value 95}
                                 {:year 1995 :value 110}
                                 {:year 1996 :value 120}
                                 {:year 1997 :value 160}
                                 {:year 1998 :value 170}
                                 {:year 1999 :value 180}
                                 {:year 2000 :value 180}
                                 {:year 2001 :value 200}]})}

  (let [{:keys [ui/react-key root/all-users root/friends root/enemies]} props]
    (dom/div
      {:key react-key :className "ui.segments"}
      (dom/div {:className "ui.top.attached.segment"}
               (dom/h3 {:className "ui.header"}
                       "Welcome to Flechar")
               (dom/p
                 "Flechar is a tool for manipulating structures. "
                 "Structures are grounded in structured collections of arrows and paths. "
                 "Primarily we are concerned with visualizations of categories, i.e. structures of arrows. "
                 "We make use of open source libraries for visualizing and editing structures. ")
               (dom/ul {:className "ui.list"}
                 (dom/li {:className "ui.item"}
                         (dom/p (dom/a {:href "http://js.cytoscape.org/"} "cytoscape")
                                " for styled 2D graph visualization. With layout algorithm. "))

                 (dom/li {:className "ui.item"}
                         (dom/p (dom/a {:href "https://deck.gl/#/"} "deck.gl")
                                " for styled graph visualization. With geographic map layouts. "))

                 (dom/li {:className "ui.item"}
                         (dom/p (dom/a {:href "https://threejs.org/"} "three js")
                                " for styled 3D graph visualization. With layout algorithm. ")))
               (dom/p
                 "Make sure you've installed Fulcro Inspect, "
                 "and your Chrome devtools will let you examine all "
                 "of the details of the running flechar!")


             (dom/div {:className "ui.attached.segment" :id "person-list" :key "person-list"}
                      (pc/ui-person-list friends)
                      (pc/ui-person-list enemies))

             (dom/div {:className "ui.attached.segment"}
                      (svg/ui-svg {:svg/w 250 :svg/h 50 :svg/label "svg"}))

             (dom/div {:className "ui.attached.segment"}
                      (svg/ui-svg {:svg/w 250 :svg/h 50 :svg/label "cytoscape"}))

             (dom/div {:className "ui.attached.segment"}
                      (svg/ui-svg {:svg/w 250 :svg/h 50 :svg/label "deck.gl"}))

             (dom/div {:className "ui.attached.segment"}
                      (svg/ui-svg {:svg/w 250 :svg/h 50 :svg/label "three.js"})))

      (dom/div {:className "ui.attached.segment"}
               (dom/div {:className "content"}
                        (uuc/ui-user-button {:db/id "user-button"
                                             :user-button/width 50 :user-button/height 50
                                             :user-button/label "avatar"})
                        (dom/div "Your system has the following users in the database:")
                        (dom/ul {:className "ui.list"}
                          (map uuc/ui-user all-users))))
      (dom/div
        (victor/ui-yearly-value-chart props)))))
