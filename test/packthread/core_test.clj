(ns packthread.core-test
  (:require [midje.sweet :refer :all]
            [packthread.core :refer :all]))

(facts "about `+>`"
  (+> 42) => 42
  (+> 42 (- 1)) => 41
  (+> 43 (- 1) (/ 21) (+ 1)) => 3
  (+> 42 inc) => 43
  (facts "about `if` inside `+>`"
    (+> 42 (if true (+ 1))) => 43
    (+> 42 (if false (+ 1))) => 42
    (+> 42 (if false (+ 1) (+ 2))) => 44
    (+> 42 (if true (+ 1) (+ 2))) => 43)
  (facts "about `if-not` inside `+>`"
    (+> 42 (if-not false (+ 1))) => 43
    (+> 42 (if-not true (+ 1))) => 42
    (+> 42 (if-not true (+ 1) (+ 2))) => 44
    (+> 42 (if-not false (+ 1) (+ 2))) => 43)
  (facts "about `if-let` inside `+>"
    (+> 42 (if-let [x 1] (+ x))) => 43)
  (facts "about `cond` inside `+>"
    (+> 42 (cond true (+ 1))) => 43
    (+> 43 (cond false (+ 1) true (- 1))) => 42
    (+> 42 (cond)) => 42
    (+> 42 (cond false (+ 1))) => 42)
  (facts "about `when` inside `+>`"
    (+> 42 (when true inc)) => 43
    (+> 42 (when true)) => 42
    (+> 42 (when false inc)) => 42
    (+> 42 (when true inc inc)) => 44)
  (facts "about `when-not` inside `+>"
    (+> 42 (when-not false inc)) => 43
    (+> 42 (when-not true inc)) => 42)
  (facts "about `when-let` inside `+>`"
    (+> 42 (when-let [x 5] (+ x))) => 47
    (+> 42 (when-let [x nil] (+ x))) => 42)
  (facts "about `do` inside `+>`"
    (+> 42 (do inc inc)) => 44))

(facts "about `+>>`"
  (+>> 42) => 42
  (+>> [1] (map inc)) => [2])
