(ns packthread.core
  (:require [clojure.core.match :refer [match]]
            [packthread.lenses :as lenses]))

(def ^:private if-like? #{'if 'if-not 'if-let 'if-some})
(def ^:private if-for-when {'when 'if
                            'when-not 'if-not
                            'when-let 'if-let
                            'when-some 'if-some})

(defmacro in
  "Threads inner expressions through a lens of value.

  lens is a function which takes two arguments: a value and a function.
  It should apply the function to a _projection_ of the value, take the
  function's result, and reassemble from that result a value which can be
  used again in the outer context.

  For example,

    (+> 42
        (in (fn [v f]
              (* 2 (f (/ v 2))))
          inc)) ;=> 42.5

  This can be thought of as 'lifting' the body expressions into the 'world
  where things are twice as large'.

  As a special case, if lens is a keyword, in assumes that value is a
  map and that sub-key are threaded through the inner expressions.

  For example,

    (+> {:hello 42}
        (in :hello
          (+ 5))) ;=> {:hello 47}

  This macro can only be used inside +> or +>>.
  "
  {:style/indent 1}
  [value lens & body]
  (throw (Exception. "packthread.core/in must be used inside `+>` or `+>>`")))

(defn- catch-clause?
  [clause]
  (and (list? clause)
       (or (= 'catch (first clause))
           (= 'finally (first clause)))))

(defn- thread-first-list
  [value form]
  (apply list (first form) value (rest form)))

(defn- thread-last-list
  [value form]
  (concat form [value]))

(defn- thread
  [thread-list value form]
  (match [form]
    [([if :guard if-like? test then] :seq)]
    (let [value-symbol (gensym)]
      `(let [~value-symbol ~value]
         (~if ~test
           ~(thread thread-list value-symbol then)
           (identity ~value-symbol))))

    [([if :guard if-like? test then else] :seq)]
    (let [value-symbol (gensym)]
      `(let [~value-symbol ~value]
         (~if ~test
           ~(thread thread-list value-symbol then)
           ~(thread thread-list value-symbol else))))

    [(['case expr & clauses] :seq)]
    (let [value-symbol (gensym)
          threaded-clauses (->> clauses
                                (partition 2)
                                (map (fn [[test branch]]
                                       [test
                                        (thread thread-list value-symbol branch)]))
                                (apply concat))]
      `(let [~value-symbol ~value]
         (case ~expr ~@threaded-clauses)))

    [(['cond & clauses] :seq)]
    (let [value-symbol (gensym)
          threaded-clauses (->> clauses
                                (partition 2)
                                (map (fn [[test branch]]
                                       [test
                                        (thread thread-list value-symbol branch)]))
                                (apply concat))
          has-else? (->> threaded-clauses
                         (partition 2)
                         (filter (fn [[test _]] (= :else test)))
                         first)
          clauses-with-else (if has-else?
                              threaded-clauses
                              (concat threaded-clauses [:else value-symbol]))]
      `(let [~value-symbol ~value]
         (cond ~@clauses-with-else)))

    [(['try & body] :seq)]
    (let [value-symbol (gensym)
          catch-clauses (drop-while (complement catch-clause?) body)
          body (take-while (complement catch-clause?) body)
          threaded-body (reduce (partial thread thread-list) value-symbol body)
          threaded-catch-clauses (->> catch-clauses
                                      (map (fn [try-clause]
                                             (match [try-clause]
                                               [(['catch exception-kind exception-name & catch-body] :seq)]
                                               (let [threaded-catch-body (reduce (partial thread thread-list) value-symbol catch-body)]
                                                 `(catch ~exception-kind ~exception-name
                                                    ~threaded-catch-body))
                                                    
                                               :else
                                               try-clause))))]
      `(let [~value-symbol ~value]
         (try
           ~threaded-body
           ~@threaded-catch-clauses)))

    [([when :guard if-for-when test & body] :seq)]
    (let [value-symbol (gensym)
          threaded-body (reduce (partial thread thread-list) value-symbol body)
          if (if-for-when when)]
      `(let [~value-symbol ~value]
         (~if ~test
           ~threaded-body
           ~value-symbol)))

    [(['do & body] :seq)]
    (reduce (partial thread thread-list) value body)

    [(['let bindings & body] :seq)]
    (let [value-symbol (gensym)
          threaded-body (reduce (partial thread thread-list) value body)
          new-bindings (concat [value-symbol value] bindings)]
      `(let [~@new-bindings]
         ~threaded-body))

    [(['in lens & body] :seq)]
    (let [outside-value-symbol (gensym)
          inside-value-symbol (gensym)
          threaded-body (reduce (partial thread thread-list) inside-value-symbol body)]
      `(let [lens# (~lenses/->lens ~lens)
             ~outside-value-symbol ~value
             ~inside-value-symbol (lens# ~outside-value-symbol)]
         (lens# ~outside-value-symbol ~threaded-body)))

    [(f :guard list?)]
    (thread-list value f)

    :else
    (list form value)))

(defmacro +>
  "Threads value through forms in much the same way as ->, except for special
  handling of the following forms:
  
  if, if-not, if-let, when, when-not, when-let:

    The value is threaded through the then and else clauses independently,
    leaving the test conditions alone.  If an else clause is missing, it is
    will be supplied as though the value had been threaded through identity
    in that case.

    For example,

      (+> 42 (if true inc)) ;=> 43
      (+> 42 (if false inc)) ;=> 42
      
    In when, when-not, and when-let forms, the value is threaded through each
    form in the body, not just the last.

  cond:

    The test clauses are left untouched and the value is threaded through
    the expr clauses of each condition.  If no :else condition was supplied,
    +> pretends as though it has been (identity), and threads the value
    through that.

    For example,

      (+> 42
          (cond
            (= 1 2)
            inc)) ;=> 42

      (+> 42
          (cond
            (= 1 1)
            dec)) ;=> 41

  do:
    
    The current expr is threaded through the body forms of the do.
  "
  {:style/indent 1}
  [value & forms]
  (reduce (partial thread thread-first-list) value forms))

(defmacro +>>
  "Threads value through forms in much the same way as ->>, except for special
  handling of several forms.  These forms are documented in (doc +>).
  "
  {:style/indent 1}
  [value & forms]
  (reduce (partial thread thread-last-list) value forms))

(defmacro fn+>
  "Like fn, except threads the first argument through the body using +> .  The
  fn parameters can be omitted, in which case the anonymous function takes one
  parameter which is threaded threaded through the body.
  "
  {:style/indent :defn}
  [& args]
  (let [arg1 (gensym)
        [original-arg1 fn-args fn-body] (if (vector? (first args))
                                          [(ffirst args)
                                           (vec (cons arg1 (rest (first args))))
                                           (rest args)]
                                          [arg1 [arg1] args])]
    `(with-meta
       (fn ~fn-args
         (let [~original-arg1 ~arg1]
           (+> ~arg1 ~@fn-body)))
       ~(meta &form))))
