(ns flechar.ui.cyto
  (:require
    ;; #?(:clj [fulcro.client.dom-server :as dom]
    ;;   :cljs [fulcro.client.dom :as dom]
    [fulcro.client.dom :as dom]
    [fulcro.client.primitives :as prim]
    [react-cytoscapejs :as cy]
    [fulcro.util :as util]))


(def elements
  [ { :data { :id "one", :label "Node 1" }, :position { :x 0, :y 0 } },
    { :data { :id "two", :label "Node 2" }, :position { :x 100, :y 0 } },
    { :data { :source "one", :target "two", :label "Edge from Node1 to Node2"}}])

(defn factory-force-children
  [class]
  (fn [props & children]
    (js/React.createElement
      class
      props
      (util/force-children children))))

(defn factory-apply
  [class]
  (fn [props & children]
    (apply js/React.createElement
           class
           props
           children)))

(prim/defsc Cyto
  "Generates an Cytoscape graph image.

  ```
  (ui-cyto {:w 50 :h 50 :label \"avatar\"})
  ```
  "
  [_ props]
  (:initial-state)

  (let [{:keys [db/id]} props]
    (let [label (or label (str w "x" h))]
      (dom/svg #js {:width w :height h}
               (dom/rect #js {:width w :height h :style #js {:fill "rgb(100,200,200)"
                                                             :strokeWidth 2
                                                             :stroke      "black"}})
               (dom/text #js {:textAnchor "middle" :x (/ w 2) :y (/ h 2)} label)))))

(def ui-svg (prim/factory Svg {:keyfn :svg/label}))
