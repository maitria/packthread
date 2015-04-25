(ns packthread.lenses-test
  (:require [midje.sweet :refer :all]
            [packthread.core :refer :all]
            [packthread.lenses :as lenses]))

(facts "about lenses/identity"
  (fact "lenses/identity does not modify the focus"
    (+> 42 (in lenses/identity dec)) => 41))

(facts "about lenses/comp"
  (fact "lenses/comp returns an identity lens when no arguments are present"
    (+> 42 (in (lenses/comp) inc)) => 43)
  (fact "lenses/comp works with only one lens"
    (+> {:foo 42} (in (lenses/comp :foo) inc)) => {:foo 43})
  (fact "lenses/comp can compose two lenses"
    (+> {:foo {:bar 42}} (in (lenses/comp :foo :bar) inc)) => {:foo {:bar 43}})
  (fact "lenses/comp can compose more than two lenses"
    (+> {:foo {:bar {:baz 42}}} (in (lenses/comp :foo :bar :baz) dec)) => {:foo {:bar {:baz 41}}}))
