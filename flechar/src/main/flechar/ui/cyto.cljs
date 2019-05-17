(ns flechar.ui.cyto
  "https://github.com/plotly/react-cytoscapejs"
  (:require
    [fulcro.client.dom :as dom]
    [fulcro.client.primitives :as prim]
    [flechar.ui.helper :as help]
    ["cytoscape" :as cytoscape]
    [taoensso.timbre :as log]))

(def elements
  [{:data {:id "one", :label "Node 1"}, :position {:x 0, :y 0}},
   {:data {:id "two", :label "Node 2"}, :position {:x 100, :y 0}},
   {:data {:source "one", :target "two", :label "Edge from Node1 to Node2"}}])

(def stylesheet
  {:width "600px" :height "600px"})

(prim/defsc CytoscapeComponent
  [this props]
  {:initial-state
   (fn [params]
     (let [{:keys [id]} params]
       {:db/id         id
        :cy/elements   elements
        :cy/stylesheet stylesheet}))

   :ident
   (fn []
     [:cytoscape/by-id :db/id])

   :query
   (fn []
     [:db/id :cy/elements :cy/stylesheet])

   :pre-merge
   (fn [env]
     (log/warn "pre-merge not implemented"))

   :initLocalState
   (fn []
     (log/info "constructor not implemented, use this")
     (set! (.-saveref this) (fn [r] (set! (.-ref this) r))))

   :shouldComponentUpdate
   (fn [next-props next-state] (log/warn "react :shouldComponentUpdate"))

   :componentDidUpdate
   (fn [prev-props prev-state] (log/warn "react :componentDidUpdate"))

   :componentDidMount
   (fn [] (log/warn "react :componentDidMount"))

   :componentWillUnmount
   (fn [] (log/warn "react :componentWillUnmount"))

   :componentDidCatch
   (fn [error info] (log/warn "react :componentDidCatch"))

   :getSnapshotBeforeUpdate
   (fn [prevProps prevState] (log/warn "react :getSnapshotBeforeUpdate"))

   :getDerivedStateFromProps
   (fn [props state] (log/warn "react :getDerivedStateFromProps"))

   :getDerivedStateFromError
   (fn [error] (log/warn "react :getDerivedStateFromError"))}

  ;; render
  ;;(cy (clj->js {:elements elements})))
  ;;(log/info "breakpoint")
  ;;(clj->js {:elements elements
  ;;         :style    {:width "600px" :height "600px"})])
  (let [{:keys [db/id cytoscape/type]} props
        recy cytoscape]
    (dom/svg #js {:width 600 :height 600}
             (dom/rect #js {:width 600 :height 600
                            :style #js {:fill        "rgb(100,200,200)"
                                        :strokeWidth 2
                                        :stroke      "black"}})
             (dom/text #js {:textAnchor "middle" :x (/ 600 2) :y (/ 600 2)} "cytoscape"))))

(def ui-cyto (prim/factory CytoscapeComponent {:keyfn :cyto/id}))
