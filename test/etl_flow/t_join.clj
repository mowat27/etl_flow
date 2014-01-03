(ns etl-flow.t-join
  (:require [midje.sweet :refer :all]
            [etl-flow.join :refer :all]))

(def input-0 #{{:id "abc" :first "Jim" :last "McTavish"}
               {:id "def" :first "Tam" :last "The bam"}
               {:id "aaa" :first "foo"  :last "bar"}
               {:id "xyz" :first "Davey" :last "Davis"}})

(def input-1 #{{:id "abc" :first "James" :last "McTavish"}
               {:id "abc" :first "Don"   :last "Williams"}
               {:id "xyz" :first "Davey" :last "Davis"}
               {:id "hij" :first "Wullie" :last "The daftie"}})

(defn run []
  (join [:id] input-0 input-1))

(fact (:matches (run)) => 
      [[{:last "Davis", :first "Davey", :id "xyz"} {:last "Davis", :first "Davey", :id "xyz"}] 
       [{:last "McTavish", :first "Jim", :id "abc"} {:last "McTavish", :first "James", :id "abc"}] 
       [{:last "McTavish", :first "Jim", :id "abc"} {:last "Williams", :first "Don", :id "abc"}]])

(fact (:unused-0 (run)) => 
      [{:last "The bam", :first "Tam", :id "def"} {:last "bar", :first "foo", :id "aaa"}])

(fact (:unused-1 (run)) => 
      [{:id "hij" :first "Wullie" :last "The daftie"}])

