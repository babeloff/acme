(ns flechar.ui.cytoscape
  "https://github.com/plotly/react-cytoscapejs"
  (:require
    [fulcro.client.dom :as dom]
    [fulcro.client.primitives :as prim]
    [flechar.ui.helper :as help]
    [oops.core :as oops]
    ["cytoscape" :default cytoscape]
    ["prop-types" :as pt]
    [taoensso.timbre :as log]))

(defn at-key [obj key]
  (if (nil? obj) (get obj key) nil))

(defn to-json [obj] (clj->js obj))
(defn for-each [arr iterator]
  (.forEach arr iterator))

(defn is-either-object-nil? [a b] (or (nil? a) (nil? b)))
(defn are-both-objects-nil? [a b] (and (nil? a) (nil? b)))

(defn do-object-hashes-differ? [a b]
  (or (is-either-object-nil? a b)
      (not= (hash a) (hash b))))

(defn do-object-property-values-differ? [a b]
  (loop [props (into [] cat [(js-keys a) (js-keys b)])]
    (if (not= (oops/oget a (first props))
              (oops/oget b (first props)))
      true
      (recur (rest props)))))

(defn shallow-obj-diff
  [a b]
  (cond
    (and (is-either-object-nil? a b) (not (are-both-objects-nil? a b))) true
    (identical? a b) false
    (or (object? a) (object? b)) (not= a b)
    (do-object-property-values-differ? a b) true
    :else false))

(defn diff-at-key? [json1 json2 diff key]
  (diff (at-key json1 key) (at-key json2 key)))

;; PATCHES

(defn patch-json [cy key val to-json]
  ((get cy key) (to-json val)))

(def properties #js ["zoom"
                     "minZoom"
                     "maxZoom"
                     "zoomingEnabled"
                     "userZoomingEnabled"
                     "pan"
                     "panningEnabled"
                     "userPanningEnabled"
                     "boxSelectionEnabled"
                     "autoungrabify"
                     "autolock"
                     "autounselectify"])


(defn patch-layout [cy layout to-json]
  (let [options (to-json layout)]
    (if (options)
      (.run (.layout cy options)))))


(defn patch-stylesheet [cy style to-json]
  (-> cy
      .style
      (.fromJson (to-json style))
      .update))

(defn cy-get-id [cy-get ele-id]
  (cy-get (cy-get ele-id "data") "id"))

(def json-keys
  ["data"
   "position"
   "selected"
   "selectable"
   "locked"
   "grabbable"
   "classes"])

(defn patch-element [cy ele-1 ele-2 to-json cy-get diff]
  (let [id (cy-get-id cy-get ele-2)
        cy-ele (.getElementById cy id)
        patch (into {} (comp
                         (filter #(diff (cy-get ele-1 %)
                                        (cy-get ele-2 %)))
                         (map #(vector % (to-json (cy-get ele-2 %)))))
                    json-keys)]

    (if (diff (cy-get ele-1 "scratch")
              (cy-get ele-2 "scratch"))
      (.scratch cy-ele (to-json (cy-get ele-2 "scratch"))))

    (if (empty? patch) (.json cy-ele patch))))



(defn patch-elements [cy eles-1 eles-2 to-json cy-get for-each diff]
  (let [eles-map-1 (into {} (map #(vector % (cy-get-id cy-get %)) eles-1))
        eles-map-2 (into {} (map #(vector % (cy-get-id cy-get %)) eles-2))

        to-rm
        (transduce (filter #(contains? eles-map-2 %))
                   #(.merge %1 (.getElementById cy %2))
                   (.collection cy)
                   eles-1)

        to-add
        (into [] (comp
                   (filter #(not (contains? eles-map-1 %)))
                   (map #(to-json %)))
              eles-2)

        to-patch
        (into [] (comp
                   (filter #(contains? eles-map-1 %))
                   (map #(vector (cy-get-id cy %) %)))
              eles-2)]

    (if (empty? to-rm) (.remove cy to-rm))
    (if (empty? to-add) (.add cy to-add))
    (doseq [[old new] to-patch]
      (patch-element cy old new to-json cy-get diff))))


(defn patch
  [cy json1 json2 diff to-json cy-get for-each]
  (if (or (identical? shallow-obj-diff diff)
          (diff-at-key? json1 json2 diff "elements"))
    (patch-elements cy
                    (at-key json1 "elements")
                    (at-key json2 "elements")
                    to-json cy-get for-each diff))

  (if (diff-at-key? json1 json2 diff "stylesheet")
    (patch-stylesheet cy
                      (at-key json2 "stylesheet")
                      to-json))

  (doseq [key properties]
    (if (diff-at-key? json1 json2 diff key)
      (patch-json cy key
                  (at-key json2 key)
                  to-json)))

  (if (diff-at-key? json1 json2 diff "layout")
    (patch-layout cy
                  (at-key json2 "layout")
                  to-json)))

(defn normalize-elements [elements]
  (if (array? elements)
    elements
    (let [{:keys [nodes edges]} elements
          nodes (if (nodes) nodes [])
          edges (if (elements) edges [])]
      (concat nodes edges))))



(defn update-cytoscape
  [this prev-props new-props]
  (let [cy (:cytoscape/impl this)
        {:keys [diff toJson get forEach]} new-props]
    (patch cy prev-props new-props diff toJson get forEach)
    (if (cy new-props) (cy new-props cy))))



;; Stateful Component

(prim/defsc CytoscapeComponent
  [this props]
  {:initial-state
   (fn [params]
     (let [{:keys [id container elements stylesheet layout]} params]
       {:db/id                id
        :cytoscape/impl       nil
        :cytoscape/container  container
        :cytoscape/elements   elements
        :cytoscape/stylesheet stylesheet
        :cytoscape/layout     layout}))

   :ident
   (fn []
     (let [{:keys [db/id]} props]
       [:cytoscape/by-id id]))

   :query
   (fn []
     [:db/id :cytoscape/impl :cytoscape/container :cytoscape/elements :cytoscape/stylesheet :cytoscape/layout])

   :pre-merge
   (fn [env]
     (log/warn "pre-merge not implemented" env))

   :initLocalState
   (fn []
     (log/info "cytoscape constructor")
     (.-displayName "CytoscapeComponent"))

   ;; :shouldComponentUpdate
   ;; (fn [next-props next-state] (log/warn "react :shouldComponentUpdate" next-props next-state))

   :componentDidUpdate
   (fn [prev-props prev-state snapshot]
     (log/info "react :componentDidUpdate" prev-props prev-state)
     (update-cytoscape this prev-props (.-props this)))

   :componentDidMount
   (fn []
     (log/info "react :componentDidMount")
     (let [global (get (.-props this) "global")
           container (js/ReactDOM.findDOMNode this)
           cy (cytoscape #js {:container container})]
       ;;(set! (.-_cy this) cy)
       (if global (set! (.-global js/window) cy))
       (update-cytoscape this nil (.-props this))))


   :componentWillUnmount
   (fn [] (log/warn "react :componentWillUnmount"))

   :componentDidCatch
   (fn [error info] (log/warn "react :componentDidCatch" error info))

   :getSnapshotBeforeUpdate
   (fn [prev-props prev-state] (log/warn "react :getSnapshotBeforeUpdate" prev-props prev-state))}

   ;;:getDerivedStateFromProps
   ;;(fn [props state]
   ;;  (log/warn "react :getDerivedStateFromProps\n" props "\n" state)
   ;;  (super props state))

   ;; :getDerivedStateFromError
   ;; (fn [error] (log/warn "react :getDerivedStateFromError" error))}

  ;; render
  (let [{id :db/id} props
        {:cytoscape/keys [class-name stylesheet]} props]
    (dom/div (clj->js {:id id :className class-name :style stylesheet}))))



(def ui-cytoscape (prim/factory CytoscapeComponent {:keyfn :cytoscape/id :instrument? true}))

(def cytoscapeDefaults 
  {:identity #(%) 
   :diff shallow-obj-diff 
   :get cy-get-id
   :to-json to-json 
   :for-each for-each
   :elements [
              { :data { :id "a", :label "Example node A" } :position {:x 0 :y 0}}
              { :data { :id "b", :label "Example node B" } :position {:x 100 :y 0}},
              { :data { :id "e", :source "a", :target "b" :label "Edge from Node1 to Node2"}}]
              ; 
   :stylesheet  
   [{:selector "node",
     :style {:label "data(label)"}}]
   
   :zoom 1 
   :pan { :x 0 :y 0}})
                        
                        
  

;; (def {:keys [pt/PropTypes.object, pt/PropTypes.number, pt/PropTypes.bool, pt/PropTypes.oneOfType, pt/PropTypes.any, pt/PropTypes.func]} pt/PropTypes)
(def cytoscapePropTypes
  {
   ;; The `id` HTML attribute of the component.
   :id                  pt/PropTypes.string

   ;; The `class` HTML attribute of the component.  Use this to set the dimensions of
   ;; the graph visualisation via a style block in your CSS file.
   :className           pt/PropTypes.string

   ;; The `style` HTML attribute of the component.  Use this to set the dimensions of
   ;; the graph visualisation if you do not use separate CSS files.
   :style               (pt/PropTypes.oneOfType #js [pt/PropTypes.string, pt/PropTypes.object])

   ;; The flat list of Cytoscape elements to be included in the graph, each represented
   ;; as non-stringified JSON.  E.g.: see default.elements
   ;;
   ;; See http://js.cytoscape.org/#notation/elements-json
   :elements            (pt/PropTypes.oneOfType #js [pt/PropTypes.array, pt/PropTypes.any])

   ;; The Cytoscape stylesheet as non-stringified JSON.  E.g.:
   ;;
   ;;```
   ;; stylesheet: [
   ;;   {
   ;;      selector: 'node',
   ;;      style: {
   ;;        'width': 30,
   ;;        'height': 30,
   ;;        'shape': 'rectangle'
   ;;}
   ;;}
   ;;]
   ;;```
   ;;
   ;; See http://js.cytoscape.org/#style
   :stylesheet          (pt/PropTypes.oneOfType #js [pt/PropTypes.array, pt/PropTypes.any])

   ;; Use a layout to automatically position the nodes in the graph.  E.g.
   ;;
   ;;```
   ;; layout: { name: 'random'}
   ;;```
   ;;
   ;; N.b. to use an external layout extension, you must register the extension
   ;; prior to rendering this component, e.g.:
   ;;
   ;;```
   ;; (require
   ;;   ["cytoscape" :default Cytoscape]
   ;;   ["cytoscape-cose-bilkent" :default COSEBilkent];
   ;;
   ;; Cytoscape.use(COSEBilkent);
   ;;
   ;; class MyApp extends React.Component {
   ;;   render() {
   ;;     const elements = [
   ;;       { data: { id: 'one', :label 'Node 1' }, position: { x: 0, y: 0 } },
   ;;       { data: { id: 'two', :label 'Node 2' }, position: { x: 100, y: 0 } },
   ;;       { data: { :source 'one', :target 'two', :label 'Edge from Node1 to Node2'}}
   ;;
   ;;
   ;;     const layout = { name: 'cose-bilkent'};
   ;;
   ;;     return <CytoscapeComponent elements={elements} layout={layout}>;
   ;;
   ;;
   ;;```
   ;;
   ;; See http://js.cytoscape.org/#layouts
   :layout              (pt/PropTypes.oneOfType #js [pt/PropTypes.object, pt/PropTypes.any])

   ;; The panning position of the graph.
   ;;
   ;; See http://js.cytoscape.org/#init-opts/pan
   :pan                 (pt/PropTypes.oneOfType #js [pt/PropTypes.object, pt/PropTypes.any])

   ;; The zoom level of the graph.
   ;;
   ;; See http://js.cytoscape.org/#init-opts/zoom
   :zoom                pt/PropTypes.number

   ;; Whether the panning position of the graph is mutable overall.
   ;;
   ;; See http://js.cytoscape.org/#init-opts/panningEnabled
   :panningEnabled      pt/PropTypes.bool

   ;; Whether the panning position of the graph is mutable by user gestures (e.g. swipe).
   ;;
   ;; See http://js.cytoscape.org/#init-opts/userPanningEnabled
   :userPanningEnabled  pt/PropTypes.bool

   ;; The minimum zoom level of the graph.
   ;;
   ;; See http://js.cytoscape.org/#init-opts/minZoom
   :minZoom             pt/PropTypes.number

   ;; The maximum zoom level of the graph.
   ;;
   ;; See http://js.cytoscape.org/#init-opts/maxZoom
   :maxZoom             pt/PropTypes.number

   ;; Whether the zoom level of the graph is mutable overall.
   ;;
   ;; See http://js.cytoscape.org/#init-opts/zoomingEnabled
   :zoomingEnabled      pt/PropTypes.bool

   ;; Whether the zoom level of the graph is mutable by user gestures (e.g. pinch-to-zoom).
   ;;
   ;; See http://js.cytoscape.org/#init-opts/userZoomingEnabled
   :userZoomingEnabled  pt/PropTypes.bool

   ;; Whether shift+click-and-drag box selection is enabled.
   ;;
   ;; See http://js.cytoscape.org/#init-opts/boxSelectionEnabled
   :boxSelectionEnabled pt/PropTypes.bool

   ;; If true, nodes automatically can not be grabbed regardless of whether
   ;; each node is marked as grabbable.
   ;;
   ;; See http://js.cytoscape.org/#init-opts/autoungrabify
   :autoungrabify       pt/PropTypes.bool

   ;; If true, nodes can not be moved at all.
   ;;
   ;; See http://js.cytoscape.org/#init-opts/autolock
   :autolock            pt/PropTypes.bool

   ;; If true, elements have immutable selection state.
   ;;
   ;; See http://js.cytoscape.org/#init-opts/autounselectify
   :autounselectify     pt/PropTypes.bool

   ;; `get(object, key)`
   ;; Get the value of the specified `object` at the `key`, which may be an integer
   ;; in the case of lists/arrays or strings in the case of maps/objects.
   :get                 pt/PropTypes.func

   ;; `toJson(object)`
   ;; Get the deep value of the specified `object` as non-stringified JSON.
   :toJson              pt/PropTypes.func

   ;; diff(objectA, objectB)
   ;; Return whether the two objects have equal value. This is used to determine if
   ;; and where Cytoscape needs to be patched.
   :diff                pt/PropTypes.func

   ;; forEach(list, iterator)
   ;; Call `iterator` on each element in the `list`, in order.
   :forEach             pt/PropTypes.func

   ;; cy(cyRef)
   ;; The `cy` prop allows for getting a reference to the `cy` Cytoscape object, e.g.:
   ;;
   ;; `<CytoscapeComponent cy={cy => (myCyRef = cy)} />`
   :cy                  pt/PropTypes.func})

