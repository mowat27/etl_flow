(ns etl-flow.transform)

(defn ==> [& args] 
  (if (= 1 (count args))
    (first args)
    args))

(defn- always-vector [x] 
  (if (vector? x) x [x]))

(defn gen-rule [row [srcs f tgt]]
  (let [assigner (fn [v] 
                 (cond  
                  (vector? tgt) (fn [m] (reduce #(assoc %1 %2 v) m tgt))
                  (= :* tgt)    (if (map? v) 
                                  (constantly v)
                                  (fn [m] (reduce #(assoc %1 %2 v) m (always-vector srcs))))
                  :else         (fn [m] (assoc m tgt v))))]
    (cond 
     (not (fn? f))   (assigner f)
     (= :* srcs)     (assigner (f row))
     (map? srcs)     (assigner (f srcs))
     (vector? srcs)  (assigner (apply f ((apply juxt srcs) row)))
     :else           (assigner (f (srcs row))))))

(defn transform [row mappings]
  (let [rules (map #(gen-rule row %1) mappings)]
    (reduce (fn [result rule] (rule result)) {} rules))) 


