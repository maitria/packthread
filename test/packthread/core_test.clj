(ns packthread.core-test
  (:require [midje.sweet :refer :all]
            [packthread.core :as p]))

(facts "about `->`"
  (p/-> 42) => 42
  (p/-> 42 (- 1)) => 41
  (p/-> 43 (- 1) (/ 21) (+ 1)) => 3
  (facts "about `if` inside `->`"
    (p/-> 42 (if true (+ 1))) => 43
    (p/-> 42 (if false (+ 1))) => 42
    (p/-> 42 (if false (+ 1) (+ 2))) => 44
    (p/-> 42 (if true (+ 1) (+ 2))) => 43)
  (facts "about `if-not` inside `->`"
    (p/-> 42 (if-not false (+ 1))) => 43
    (p/-> 42 (if-not true (+ 1))) => 42
    (p/-> 42 (if-not true (+ 1) (+ 2))) => 44
    (p/-> 42 (if-not false (+ 1) (+ 2))) => 43))
