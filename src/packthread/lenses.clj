(ns packthread.lenses)

(defn under
  "A lens which focuses the value under `kw` in the map"
  [kw]
  (fn [v f]
    (update-in v [kw] f)))
