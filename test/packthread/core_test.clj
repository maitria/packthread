(ns packthread.core-test
  (:require [midje.sweet :refer :all]
            [packthread.core :as p]))

(facts "about `=>`"
  (p/x> 42) => 42
  (p/x> 42 (- 1)) => 41)
