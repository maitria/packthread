(ns packthread.core
  (:refer-clojure :exclude [->]))

(defmacro ->
  [value & forms]
  (loop [forms forms
         result value]
    (if-let [[form rest-of-forms] forms]
      (recur rest-of-forms
             (apply list (first form) result (rest form)))
      result)))
