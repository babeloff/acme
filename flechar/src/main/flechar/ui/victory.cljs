(ns flechar.ui.victory
    (:require
      [cljs.pprint :refer [cl-format]]
      ;; REQUIRES shadow-cljs, with "victory" in package.json
      ["victory" :refer [VictoryChart VictoryAxis VictoryLine]]
      [fulcro.client.cards :refer [defcard-fulcro]]
      [fulcro.client.dom :as dom]
      [fulcro.client.primitives :as prim :refer [defsc]]
      [fulcro.util :as util]))

(defn us-dollars [n]
      (str "$" (cl-format nil "~:d" n)))

(defn factory-force-children
      [class]
      (fn [props & children]
        (js/React.createElement class
                                props
                                (util/force-children children))))

(defn factory-apply
      [class]
      (fn [props & children]
        (apply js/React.createElement
               class
               props
               children)))

(def vchart (factory-apply VictoryChart))
(def vaxis (factory-apply VictoryAxis))
(def vline (factory-apply VictoryLine))

;; " [ {:year 1991 :value 2345 } ...] "
(defsc YearlyValueChart [this {:keys [label plot-data x-step]}]
       (let [start-year (apply min (map :year plot-data))
             end-year   (apply max (map :year plot-data))
             years      (range start-year (inc end-year) x-step)
             dates      (clj->js (mapv #(new js/Date % 1 2) years))
             {:keys [min-value
                     max-value]} (reduce (fn [{:keys [min-value max-value] :as acc}
                                              {:keys [value] :as n}]
                                           (assoc acc
                                             :min-value (min min-value value)
                                             :max-value (max max-value value)))
                                         {}
                                         plot-data)
             min-value  (int (* 0.8 min-value))
             max-value  (int (* 1.2 max-value))
             points     (clj->js (mapv (fn [{:keys [year value]}]
                                         {:x (new js/Date year 1 2)
                                          :y value})
                                       plot-data))]
         (vchart nil
                 (vaxis #js {:label      label
                             :standalone false
                             :scale      "time"
                             :tickFormat (fn [d] (.getFullYear d))
                             :tickValues dates})
                 (vaxis #js {:dependentAxis true
                             :standalone    false
                             :tickFormat    (fn [y] (us-dollars y))
                             :domain        #js [min-value max-value]})
                 (vline #js {:data points}))))

(def ui-yearly-value-chart (prim/factory YearlyValueChart))

