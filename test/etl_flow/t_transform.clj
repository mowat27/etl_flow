(ns etl-flow.t-transform
  (:require [midje.sweet :refer :all]
            [etl-flow.transform :refer :all]))

(facts "about transform with a single, named source and target fields" 
       (transform {:a 1 :b 2} [[:a ==> :a]
                               [:b ==> :b]
                               [:a ==> :c]]) => {:a 1 :b 2 :c 1}

       (transform {:a 1 :b 2} [[:a ==> :c]]) => {:c 1}
       (transform {:a 1 :b 2} [[:a inc :a]]) => {:a 2}
       (transform {:a 1 :b 2} [[:a ==> :x]]) => {:x 1}

       (transform {"a" 1 "b" 2} [["a" ==> "x"]]) => {"x" 1}
       (transform {1 "a" 2 "b"} [[1 ==> 9]])     => {9 "a"}
       (transform {\a 1 \b 2}   [[\a ==> \c]])   => {\c 1}
)

(facts "about transform with multiple source fields"
       (transform {:a 1 :b 2} [[[:a :b] +   :c]]) => {:c 3}
       (transform {:a 1 :b 2} [[[:a :b] ==> :c]]) => {:c [1 2]}

       (transform {:a 1 :b 2} [['(:a :b) +   :c]]) => {:c 3}
       (transform {:a 1 :b 2} [['(:a :b) ==> :c]]) => {:c [1 2]}
       (transform {:a 1 :b 2} [[#{:a :b} +   :c]]) => {:c 3}
       (transform {:a 1 :b 2} [[#{:a :b} ==> :c]]) => {:c [1 2]}

       (transform {"a" 1 "b" 2} [[["a" "b"] + "x"]]) => {"x" 3}
       (transform {1 "a" 2 "b"} [[[1 2]    str 9]])  => {9 "ab"}
       (transform {\a 1 \b 2}   [[[\a \b]   + \c]])  => {\c 3})


(facts "about transform when assigning a static value to a target"
       (transform {:a 1 :b 2} [[:a        "hello" :x]])  => {:x "hello"}
       (transform {:a 1 :b 2} [[nil       "hello" :x]])  => {:x "hello"} 
       (transform {:a 1 :b 2} [[:anything "hello" :x]])  => {:x "hello"}
       (transform {:a 1 :b 2} [[:anything "hello" "a"]]) => {"a" "hello"}) 

(facts "about transform when assigning from a map"
       (transform {:a 1 :b 2} [[{:c 3} ==> :x]]) => {:x {:c 3}}
       (transform {:a 1 :b 2} [[{:c 3} #(assoc % :a "foo") :x]]) => {:x {:a "foo" :c 3}})

(facts "about transform when assigning from all fields"
       (transform {:a 1 :b 2} [[:* ==> :row]]) => {:row {:a 1 :b 2}}
       (transform {:a 1 :b 2} [[:* ==> :row]
                               [:b ==> :val]]) => {:row {:a 1 :b 2} :val 2}
       (transform {:a 1 :b 2} [[:* #(assoc % :a "foo") :x]]) => {:x {:a "foo" :b 2}})

(facts "about assigning to multiple fields"
       (transform {:a 1 :b 2} [[:a ==> [:c :d]]]) => {:c 1 :d 1})

(facts "about transform when assigning to all fields"
       (transform {:a 1 :b 2} [[:a ==> :*]])    => {:a 1}
       (transform {:a 1 :b 2} [[:a inc :*]])    => {:a 2}
       (transform {:a 1 :b 2} [[[:a :b] + :*]]) => {:a 3 :b 3}
       (transform {:a 1 :b 2} [['(:a :b) + :*]]) => {:a 3 :b 3}
       )

(facts "about transform when assigning from and to 'all fields'"
       (transform {:a 1 :b 2} [[:* ==> :*]])   => {:a 1 :b 2}
       (transform {:a 1 :b 2} [[:* #(assoc % :a "foo") :*]]) => {:a "foo" :b 2}
       (transform {:a 1 :b 2} [[:* ==> :*]
                               [:b inc :b]])   => {:a 1 :b 3})
  
