(ns packthread.core-test
  (:require [midje.sweet :refer :all]
            [packthread.core :as p]))

(facts "about `->`"
  (p/-> 42) => 42
  (p/-> 42 (- 1)) => 41
  (p/-> 43 (- 1) (/ 21) (+ 1)) => 3
  (p/-> 42 inc) => 43
  (facts "about `if` inside `->`"
    (p/-> 42 (if true (+ 1))) => 43
    (p/-> 42 (if false (+ 1))) => 42
    (p/-> 42 (if false (+ 1) (+ 2))) => 44
    (p/-> 42 (if true (+ 1) (+ 2))) => 43)
  (facts "about `if-not` inside `->`"
    (p/-> 42 (if-not false (+ 1))) => 43
    (p/-> 42 (if-not true (+ 1))) => 42
    (p/-> 42 (if-not true (+ 1) (+ 2))) => 44
    (p/-> 42 (if-not false (+ 1) (+ 2))) => 43)
  (facts "about `if-let` inside `->"
    (p/-> 42 (if-let [x 1] (+ x))) => 43)
  (facts "about `cond` inside `->"
    (p/-> 42 (cond true (+ 1))) => 43
    (p/-> 43 (cond false (+ 1) true (- 1))) => 42
    (p/-> 42 (cond)) => 42
    (p/-> 42 (cond false (+ 1))) => 42)
  (facts "about `when` inside `->`"
    (p/-> 42 (when true inc)) => 43
    (p/-> 42 (when true)) => 42
    (p/-> 42 (when false inc)) => 42))
