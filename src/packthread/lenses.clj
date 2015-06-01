(ns packthread.lenses
  (:refer-clojure :exclude [comp identity]))

(defn under
  "A lens which focuses the value under `kw` in the map"
  [kw]
  (fn 
    ([v] (get v kw))
    ([v u] (assoc v kw u))))

(defn ->lens
  "Coerce thing to a lens.  If it is a keyword, the lens is (under thing);
  otherwise, assume thing is already a lens."
  [thing]
  (cond-> thing
    (keyword? thing) under))

(defn identity
  "The identity lens: f is applied to v."
  ([v] v)
  ([v u] u))

(defn comp
  "Compose multiple lenses - analogous to functional composition.
  
  If no lenses are passed, the result is the identity lens; otherwise, lenses
  are composed so the left most is the outer-most (it runs first in the
  \"get\" direction and last in the \"putback\" direction."
  ([] identity)
  ([a] (->lens a))
  ([a b] 
   (let [a (->lens a)
         b (->lens b)]
     (fn 
       ([v] (b (a v)))
       ([v u] (a v (b (a v) u))))))
  ([a b & cs]
   (reduce comp (concat [a b] cs))))
