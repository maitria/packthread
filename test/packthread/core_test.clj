(ns packthread.core-test
  (:require [midje.sweet :refer :all]
            [packthread.core :as pt]))

(facts "about `->`"
  (pt/-> 42) => 42)
