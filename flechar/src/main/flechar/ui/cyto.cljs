(ns flechar.ui.cyto
  "https://github.com/plotly/react-cytoscapejs"
  (:require
    [fulcro.client.dom :as dom]
    [fulcro.client.primitives :as prim]
    [flechar.ui.helper :as help]
    ["react-cytoscapejs" :as recy]))


(def elements
  [{:data {:id "one", :label "Node 1"}, :position {:x 0, :y 0}},
   {:data {:id "two", :label "Node 2"}, :position {:x 100, :y 0}},
   {:data {:source "one", :target "two", :label "Edge from Node1 to Node2"}}])

(def cytoscape-component (help/factory-default recy))

(prim/defsc Cyto
  [_ props]
  (cytoscape-component (clj->js {:elements elements
                                 :style    {:width "600px" :height "600px"}})))


(def ui-cyto (prim/factory Cyto {:keyfn :cyto/id}))
