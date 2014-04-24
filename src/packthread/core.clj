(ns packthread.core
  (:refer-clojure :exclude [->]))

(defmulti thread (fn [value form] (first form)))

(defmethod thread :default
  [value form]
  (apply list (first form) value (rest form)))

(defmethod thread 'if
  [value form]
  (let [if-test (second form)
        value-symbol (gensym)
        then (nth form 2)
        else (if (= 4 (count form))
               (nth form 3)
               '(identity))
        threaded-then (thread value-symbol then)
        threaded-else (thread value-symbol else)]
    `(let [~value-symbol ~value]
       (if ~if-test
         ~threaded-then 
         ~threaded-else))))

(defmacro ->
  [value & forms]
  (reduce thread value forms))
