(ns etl-flow.transform)

(defn ==> [& args] 
  (if (= 1 (count args))
    (first args)
    args))

(defn- list-like? [x]
  (or (vector? x)
      (list? x)
      (set? x)))

(defn- always-vector [x] 
  (if (list-like? x) (vec x) [x]))

(defn- gen-rule [row [src f tgt]]
  (let [assigner (fn [v] 
                   (cond  
                    (vector? tgt) (fn [m] (reduce #(assoc %1 %2 v) m tgt))
                    (= :* tgt)    (if (map? v) 
                                    (constantly v)
                                    (fn [m] (reduce #(assoc %1 %2 v) m (always-vector src))))
                    :else         (fn [m] (assoc m tgt v))))]
    (cond 
     (not (fn? f))    (assigner f)
     (= :* src)       (assigner (f row))
     (map? src)       (assigner (f src))
     (list-like? src) (assigner (apply f (for [field src] (get row field))))
     :else            (assigner (f (get row src))))))


(defn transform 
  "Applys mappings to row"
  [row mappings]
  (let [rules (map #(gen-rule row %1) mappings)]
    (reduce (fn [result rule] (rule result)) {} rules))) 
