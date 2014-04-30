(ns packthread.core
  (:require [clojure.core.match :refer [match]]))

(def ^:private if-like? #{'if 'if-not 'if-let})
(def ^:private if-for-when {'when 'if
                            'when-not 'if-not
                            'when-let 'if-let})

(defmacro in
  [& args]
  (throw (Exception. "packthread.core/in must be used inside `+>` or `+>>`")))

(defn -lift-into-projection
  [value projector into-fn]
  (let [projector (if (keyword? projector)
                    #(update-in %1 [projector] %2)
                    projector)]
    (projector value into-fn)))

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

    [(['in projector & body] :seq)]
    (let [projection-symbol (gensym)
          threaded-body (reduce (partial thread thread-list) projection-symbol body)]
      `(-lift-into-projection ~value ~projector
         (fn [~projection-symbol]
           ~threaded-body)))

    [(f :guard list?)]
    (thread-list value f)

    :else
    (list form value)))

(defmacro +>
  [value & forms]
  (reduce (partial thread thread-first-list) value forms))

(defmacro +>>
  [value & forms]
  (reduce (partial thread thread-last-list) value forms))
