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
  (facts "about `if-some` inside `+>`"
    (+> 42 (if-some [x nil] inc)) => 42
    (+> 42 (if-some [x false] inc)) => 43
    (+> 42 (if-some [x 2] (+ x))) => 44
    (+> 42 (if-some [x nil] inc dec)) => 41
    (+> 42 (if-some [x false] inc dec)) => 43
    (+> 42 (if-some [x 2] (+ x) dec)) => 44)
  (facts "about `let` inside `+>`"
    (+> 42 (let [x 1] (+ x))) => 43)
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
  (facts "about `when-some` inside `+>"
    (+> 42 (when-some [x 5] (+ x))) => 47
    (+> 42 (when-some [x nil] (+ x))) => 42)
  (facts "about `do` inside `+>`"
    (+> 42 (do inc inc)) => 44)
  (facts "about `try` inside `+>`"
    (+> 42 (try inc inc)) => 44)
  (+> 42 (try (+ :hello) (catch Exception e dec))) => 41
  (+> 42 (try inc (finally 45))) => 43
  (facts "about `case` inside `+>`"
    (+> 42 (case 1 1 (+ 1) 2 (- 1))) => 43
    (+> 42 (case 2 1 (+ 1) 2 (- 1))) => 41))

(facts "about `+>>`"
  (+>> 42) => 42
  (+>> [1] (map inc)) => [2]
  (+>> [1] (if true (map inc) (map dec))) => [2])

(defn weirdo-land
  ([value] (* 2 (:hello value)))
  ([value putback] (assoc value :hello (/ putback 2))))

(facts "about `in`"
  (+> {:hello 42} (in :hello inc)) => {:hello 43}
  (+> {:hello 42} (in weirdo-land inc)) => {:hello 85/2}
  (+>> {:hello [42]}
       (in :hello
         (map inc))) => {:hello [43]}
  (+> 42
      (in (fn
            ([v] (/ v 2))
            ([v u] (* u 2)))
        inc)) => 44)

(facts "about `fn+>`"
  ((fn+>) 42) => 42
  ((fn+> [arg]) 79) => 79
  ((fn+> (+ 1)) 42) => 43
  ((fn+> [x] (+ x)) 7) => 14
  ((fn+> inc inc) 42) => 44
  (fact "metadata is passed through"
    (meta ^{:foo :bar} (fn+> inc)) => (contains {:foo :bar}))
  (fact "works with destructuring arguments"
    ((fn+> [{:keys [foo]}]
       (assoc :foo 79)
       (merge {:bar (inc foo)}))
     {:foo 42}) => (contains {:bar 43})))
