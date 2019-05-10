(ns flechar.ui.root
  (:require
    ;; #?(:clj [fulcro.client.dom-server :as dom]
    ;;   :cljs [fulcro.client.dom :as dom]
    [fulcro.client.dom :as dom]
    [fulcro.client.primitives :as prim]
    [flechar.ui.person-comp :as pc]
    [flechar.ui.svg_comp :as svg]
    [flechar.ui.user-comp :as uuc]
    [taoensso.timbre :as log]))


(prim/defsc Root [this {:keys [all-users friends enemies]}]
  {:query         [{:all-users (prim/get-query uuc/User)}
                   {:friends (prim/get-query pc/PersonList)}
                   {:enemies (prim/get-query pc/PersonList)}]
   :initial-state (fn [_]
                    {:all-users (prim/get-initial-state uuc/User {})
                     :friends   (prim/get-initial-state pc/PersonList {:label "Friends"})
                     :enemies   (prim/get-initial-state pc/PersonList {:label "Enemies"})})}
  (log/debug :query this)
  (dom/div :.ui.segments
    (dom/div :.ui.top.attached.segment
             (dom/h3 :.ui.header
                     "Welcome to Flechar")
             (dom/p
               "Flechar is a tool for manipulating structures. "
               "Structures are grounded in structured collections of arrows and paths. "
               "Primarily we are concerned with visualizations of categories, i.e. structures of arrows. "
               "We make use of open source libraries for visualizing and editing structures. ")
             (dom/ul
               (dom/li :.ui.item
                       (dom/p (dom/a {:href "http://js.cytoscape.org/"} "cytoscape")
                              " for styled 2D graph visualization. With layout algorithm. "))

               (dom/li :.ui.item
                       (dom/p (dom/a {:href "https://deck.gl/#/"} "deck.gl")
                              " for styled graph visualization. With geographic map layouts. "))

               (dom/li :.ui.item
                       (dom/p (dom/a {:href "https://threejs.org/"} "three js")
                              " for styled 3D graph visualization. With layout algorithm. ")))
             (dom/p
               "Make sure you've installed Fulcro Inspect, "
               "and your Chrome devtools will let you examine all "
               "of the details of the running flechar!")


           (dom/div :.ui.attached.segment
                    (pc/ui-person-list friends)
                    (pc/ui-person-list enemies))

           (dom/div :.ui.attached.segment
                           (svg/ui-svg {:w 250 :h 50 :label "svg"}))

           (dom/div :.ui.attached.segment
                           (svg/ui-svg {:w 250 :h 50 :label "cytoscape"}))

           (dom/div :.ui.attached.segment
                           (svg/ui-svg {:w 250 :h 50 :label "deck.gl"}))

           (dom/div :.ui.attached.segment
                           (svg/ui-svg {:w 250 :h 50 :label "three.js"})))

    (dom/div :.ui.attached.segment
      (dom/div :.content
               (dom/div "Your system has the following users in the database:")
               (dom/ul :.ui.list
                       (map uuc/ui-user all-users))
               (uuc/ui-user-button {:w 50 :h 50 :label "avatar"})))))
