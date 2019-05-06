(ns flechar.sample-test
  (:require
    [clojure.test :as ct]
    [fulcro-spec.core :as fsc]))

; Tests for both client and server
(ct/deftest
  sample-test
  (fsc/behavior
    "addition computes addition correctly"
    (fsc/assertions
      "with positive integers"
      (+ 1 5 3) => 9
      "with negative integers"
      (+ -1 -3 -5) => -9
      "with a mix of signed integers"
      (+ +5 -3) => 2)))
