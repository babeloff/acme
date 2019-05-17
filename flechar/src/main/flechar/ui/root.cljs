(ns flechar.ui.root
  (:require
    ;; #?(:clj  [fulcro.client.dom-server :as dom]
    ;;    :cljs [fulcro.client.dom :as dom])
    [fulcro.client.dom :as dom]
    [fulcro.client.primitives :as prim]
    [flechar.ui.person :as pc]
    [flechar.ui.svg :as svg]
    [flechar.ui.user :as uuc]
    [flechar.ui.victory :as vic]
    [flechar.ui.cyto :as cyto]
    [taoensso.timbre :as log]))


(prim/defsc Root [_ props]

  {:query
   [:ui/react-key
    {:root/all-users (prim/get-query uuc/User)}
    {:root/friends (prim/get-query pc/PersonList)}
    {:root/enemies (prim/get-query pc/PersonList)}
    {:root/charts (prim/get-query vic/VictorChartSet)}]

   :initial-state
   (fn [_]
     {:ui/react-key   "initial"
      :root/all-users (prim/get-initial-state uuc/User
                                              {:id     "my-self"
                                               :name   "Fred"
                                               :street "Music Row"
                                               :city   "Nashville"})
      :root/friends   (prim/get-initial-state pc/PersonList {:id :friends :label "Friends"})
      :root/enemies   (prim/get-initial-state pc/PersonList {:id :enemies :label "Enemies"})

      :root/charts    (prim/get-initial-state vic/VictorChartSet {})})

   :componentDidCatch
   (fn [error info] (js/console.log "root :componentDidCatch" error info))


   :getDerivedStateFromError
   (fn [error] (js/console.log "root :getDerivedStateFromError" error))}

  (let [{:keys [ui/react-key root/all-users root/friends root/enemies root/charts]} props]
    (log/info "starting root")
    (dom/div
      {:key react-key :className "ui.segments"}
      (dom/div
        {:className "ui.top.attached.segment"}
        (dom/h3 {:className "ui.header"}
                "Welcome to Flechar")
        (dom/p
          "Flechar is a tool for manipulating structures. "
          "Structures are grounded in structured collections of arrows and paths. "
          "Primarily we are concerned with visualizations of categories, i.e. structures of arrows. "
          "We make use of open source libraries for visualizing and editing structures. ")
        (dom/ul {:className "ui.list" :key "visualization-type-list"}
                (dom/li {:className "ui.item"}
                        (dom/p (dom/a {:href "https://formidable.com/open-source/victory/"} "victory")
                               " for styled 2D charts. "))

                (dom/li {:className "ui.item"}
                        (dom/p (dom/a {:href "https://github.com/psychobolt/react-pie-menu"} "pie-menu")
                               " for contextual menus "))

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
          "of the details of the running flechar!"))


      (dom/div {:className "ui.attached.segment" :id "person-list" :key "person-list"}
               (pc/ui-person-list friends)
               (pc/ui-person-list enemies))


      (dom/div {:className "ui.attached.segment" :key "user-button-segment"}
               (dom/div {:className "content" :key "user-button-content"}
                        (uuc/ui-user-button
                          {:db/id             "user-button"
                           :user-button/width 50 :user-button/height 50
                           :user-button/label "avatar"})
                        (dom/div {:key "user-list-label"}
                                 "Your system has the following users in the database:")
                        (dom/ul {:className "ui.list" :key "user-list"}
                                (log/info "all-users " (count all-users))
                                (map uuc/ui-user all-users))))

      (dom/div {:className "ui.attached.segment" :id "chart-set" :key "chart-set"}
               (vic/ui-victor-charts charts))

      (dom/div {:className "ui.attached.segment"}
               (svg/ui-svg {:svg/w 250 :svg/h 50 :svg/label "svg"}))

      (dom/div {:className "ui.attached.segment"}
               (cyto/ui-cyto {:svg/w 250 :svg/h 50 :svg/label "cytoscape"}))

      (dom/div {:className "ui.attached.segment"}
               (svg/ui-svg {:svg/w 250 :svg/h 50 :svg/label "deck.gl"}))

      (dom/div {:className "ui.attached.segment"}
               (svg/ui-svg {:svg/w 250 :svg/h 50 :svg/label "three.js"})))))
