(ns packthread.core
  (:refer-clojure :exclude [->]))

(defn- thread
  [value form]
  (apply list (first form) value (rest form)))

(defmacro ->
  [value & forms]
  (reduce thread value forms))
