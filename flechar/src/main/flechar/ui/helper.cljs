(ns flechar.ui.helper)

;; http://book.fulcrologic.com/#_factory_functions_for_js_react_components

(defn factory-apply
  [class]
  (fn [props & decend]
    (apply js/React.createElement
           class
           props
           decend)))

(defn factory-default
  [package]
  (factory-apply (.-default package)))
