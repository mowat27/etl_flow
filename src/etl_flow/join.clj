(ns etl-flow.join
  (:require [clojure.data :refer [diff]]
            [clojure.set :as set]))

(defn filter-index [idx ks]
  (mapv (partial apply hash-map) (filter (fn [[kv _]] (contains? ks kv)) idx)))

(defn group-by-key [& coll]
  (apply merge-with (fn [x y] 
                      (if (vector? x) 
                        (conj x y) 
                        [x y])) 
         (map #(apply merge %) coll)))

(defn unindex [idx] 
  (vec (reduce #(set/union %1 (first %2)) #{} (mapv #(vals %) idx))))

(defn product [coll] 
  (->> (map (fn [[kv rows]] rows) coll)
       (mapcat (fn [[x-rows y-rows]] 
             (for [x x-rows y y-rows]
               [x y])))
       vec))

(defn join [key old new & {:keys [unused-old-fn unused-new-fn match-fn] :as opts}]
  (let [indexes (map #(set/index % key) [old new])
        [unmatched-keys-0 unmatched-keys-1 matched-keys] (apply diff (map #(set (keys %)) indexes))]
    {:matches  (product (apply group-by-key (map #(filter-index % matched-keys) indexes)))
     :unused-0 (unindex (filter-index (first indexes) unmatched-keys-0))
     :unused-1 (unindex (filter-index (second indexes) unmatched-keys-1))}))
