(ns etl-flow.flow-play
  (:require [etl-flow.join :refer [join]]
            [etl-flow.transform :refer :all]
            [clojure.string :as str]))

(def chefs [{:id 1 :first "Michelle"     :last "Roux Jnr"}
            {:id 3 :first "Marco-Pierre" :last "White"}
            {:id 4 :first "gordon"       :last "Ramsey"}
            {:id 2 :first "Hugh"         :last "Fearnley-Whittingstall"}])

(defn upcase-first [[first & rest]]
  (str/join (conj rest (str/upper-case first))))

; (upcase-first "hello")

(defn now [] (System/currentTimeMillis))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(def logs  (agent []))

(defn transform-logger [id mappings]
  (fn [v] (conj v {:id id, :op :created, :mappings mappings})))

(defn row-logger [id m]
  (fn [v] (conj v (merge {:id id :op :row-log} m))))

(defn transformer [mappings]
  (let [id (uuid)]
    (send logs (transform-logger id mappings))
    (fn [row] 
      (let [start  (now)
            result (transform row mappings)
            end    (now)] 
        (send logs (row-logger id {:start start, :end end}))
        result))))

(def cleanse (transformer 
              [[:*     ==>           :*]
               [:first upcase-first :first]
               [:last  upcase-first :last]]))

(def add-name (transformer
               [[:*             ==>                :*]
                [[:first :last] #(format "%s %s" %1 %2) :name]]))

(->> (map cleanse chefs) (map add-name) (filter #(= 4 (:id %))))
(->> (map (comp add-name cleanse) chefs) (filter #(= 4 (:id %))))

(defn run [data & ops]
  (->> data 
       (map (apply comp (reverse ops)))))

(run chefs cleanse add-name)

(pr-str chefs)

(defn summarise-transforms [[id log-entries]] 
  {:id id 
   :row-count  (count log-entries) 
   :elapsed-ms (- (:end (last log-entries)) (:start (first log-entries)))})

(map summarise-transforms (group-by :id @logs))








