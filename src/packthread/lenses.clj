(ns packthread.lenses)

(defn under
  "A lens which focuses the value under `kw` in the map"
  [kw]
  (fn [v f]
    (update-in v [kw] f)))

(defn ->lens
  "Coerce thing to a lens.  If it is a keyword, the lens is (under thing);
  otherwise, assume thing is already a lens."
  [thing]
  (cond-> thing
    (keyword? thing) under))
