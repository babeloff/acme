(ns flechar.ui.cytoscape
  "https://github.com/plotly/react-cytoscapejs"
  (:require
    [fulcro.client.dom :as dom]
    [fulcro.client.primitives :as prim]
    [flechar.ui.helper :as help]
    [oops.core :as oops]
    ["cytoscape" :as cytoscape]
    [taoensso.timbre :as log]))

(defn is-either-object-nil? [a b] (or (nil? a) (nil? b)))
(defn are-both-objects-nil? [a b] (and (nil? a) (nil? b)))

(defn do-object-hashes-differ? [a b]
  (or (is-either-nil? a b)
      (not= (hash a) (hash b))))

(defn do-object-property-values-differ? [a b]
  (loop [props (into [] cat [(js-keys a) (js-keys b)])]
    (if (not= (oops/oget a (first prop))
              (oops/oget b (first prop)))
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

(defn is-diff-at-key [json1 json2 diff key]
  (diff (at-key json1 key) (at-key json2 key)))

;; PATCHES

(defn patch-json [cy val to-json]
  (oops/oset cy prop (to-json val)))

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


(defn patch-elements [cy style to-json get for-each diff]
  (-> cy
      .style
      (.fromJson (to-json style))
      .update))



(defn patch
  [cy json1 json2 diff to-json get for-each]
  (if (or (identical? shallow-obj-diff diff)
          (is-diff-at-key? json1 json2 diff "elements"))
    (patch-elements cy
                    (at-key json2 "elements")
                    to-json get for-each diff))

  (if (is-diff-at-key? json1 json2 diff "stylesheet")
    (patch-stylesheet cy
                      (at-key json2 "stylesheet")
                      to-json))

  (doseq [prop properties]
    (if (is-diff-at-key? json1 json2 diff prop)
      (patch-json cy
                  (at-key json2 prop)
                  to-json)))

  (if (is-diff-at-key? json1 json2 diff "layout")
    (patch-layout cy
                  (at-key json2 "layout")
                  to-json)))


(defn update-cytoscape
  [this prev-props new-props]
  (let [cy (._cy this)
        {:keys [diff toJson get forEach]} new-props]
    (patch cy prev-props new-props diff toJson get forEach)
    (if (cy new-props) (cy new-props cy))))

(prim/defsc CytoscapeComponent
  [this props]
  {:initial-state
   (fn [params]
     (let [{:keys [id container elements stylesheet layout]} params]
       {:db/id                id
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
     [:db/id :cytoscape/container :cytoscape/elements :cytoscape/stylesheet :cytoscape/layout])

   :pre-merge
   (fn [env]
     (log/warn "pre-merge not implemented" env))

   :initLocalState
   (fn []
     (log/info "cytoscape constructor")
     (update this nu))
   ;; (set! (.-saveref this) (fn [r] (set! (.-ref this) r))))

   ;; :shouldComponentUpdate
   ;; (fn [next-props next-state] (log/warn "react :shouldComponentUpdate" next-props next-state))

   :componentDidUpdate
   (fn [prev-props prev-state snapshot]
     (log/warn "react :componentDidUpdate" prev-props prev-state)
     (updateCytoscape this))

   :componentDidMount
   (fn []
     (log/info "react :componentDidMount")
     (let [container (ReactDom/findDOMNode this)
           global (.props this)
           cy (._cy .Cytoscape {container})]
       (if global (set! window [global] cy))
       (updateCytoscape this null (.props this))))


   :componentWillUnmount
   (fn [] (log/warn "react :componentWillUnmount"))

   :componentDidCatch
   (fn [error info] (log/warn "react :componentDidCatch" error info))

   :getSnapshotBeforeUpdate
   (fn [prev-props prev-state] (log/warn "react :getSnapshotBeforeUpdate" prev-props prev-state))

   :getDerivedStateFromProps
   (fn [props state] (log/warn "react :getDerivedStateFromProps" props state))

   :getDerivedStateFromError
   (fn [error] (log/warn "react :getDerivedStateFromError" error))}

  ;; render
  (let [{id :db/id} props
        {:cytoscape/keys [class-name stylesheet]} props]
    (dom/div (clj->js {:id id :className class-name :style stylesheet}))))

(def ui-cytoscape (prim/factory CytoscapeComponent {:keyfn :cytoscape/id :instrument? true}))
