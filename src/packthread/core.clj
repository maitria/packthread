(ns packthread.core
  (:refer-clojure :exclude [->])
  (:require [clojure.core.match :refer [match]]))

(def if-like? #{'if 'if-not 'if-let})

(defn thread
  [value form]
  (match [form]
    [([if :guard if-like? test then] :seq)]
    (let [value-symbol (gensym)]
      `(let [~value-symbol ~value]
         (~if ~test
           ~(thread value-symbol then)
           (identity ~value-symbol))))

    [([if :guard if-like? test then else] :seq)]
    (let [value-symbol (gensym)]
      `(let [~value-symbol ~value]
         (~if ~test
           ~(thread value-symbol then)
           ~(thread value-symbol else))))

    [([f & r] :seq :guard list?)]
    (apply list f value r)))

(defmacro ->
  [value & forms]
  (reduce thread value forms))
