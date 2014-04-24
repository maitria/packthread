(ns packthread.core-test
  (:require [midje.sweet :refer :all]
            [packthread.core :as p]))

(facts "about `->`"
  (p/-> 42) => 42
  (p/-> 42 (- 1)) => 41
  (p/-> 43 (- 1) (/ 21) (+ 1)) => 3)
