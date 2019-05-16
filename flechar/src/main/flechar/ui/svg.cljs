(ns flechar.ui.svg
  (:require
    ;; #?(:clj [fulcro.client.dom-server :as dom]
    ;;   :cljs [fulcro.client.dom :as dom]
    [fulcro.client.dom :as dom]
    [fulcro.client.primitives :as prim]))

(prim/defsc Svg
  "Generates an SVG image placeholder of the given size and with the given label
  (defaults to showing 'w x h').

  ```
  (ui-placeholder {:w 50 :h 50 :label \"avatar\"})
  ```
  "
  [_ props]

  (let [{:keys [db/id svg/w svg/h svg/label]} props]
    (let [label (or label (str w "x" h))]
      (dom/svg #js {:width w :height h}
               (dom/rect #js {:width w :height h :style #js {:fill        "rgb(100,200,200)"
                                                             :strokeWidth 2
                                                             :stroke      "black"}})
               (dom/text #js {:textAnchor "middle" :x (/ w 2) :y (/ h 2)} label)))))

(def ui-svg (prim/factory Svg {:keyfn :svg/label}))
