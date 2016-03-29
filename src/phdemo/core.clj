(ns phdemo.core
  (:require [clojure.java.io :as io]
            [thi.ng.geom.aabb :as a]
            [thi.ng.geom.mesh.io :as mio]
            [thi.ng.geom.core :as g]
            [thi.ng.geom.core.vector :as v :refer [vec3]]
            [thi.ng.morphogen.core :as mg])
  (:import [thi.ng.morphogen.core BoxNode])
  (:gen-class))

(defn make-stripes
  "Returns a tree which subdivides form into `n` columns and only
  keeps those for whose index the given predicate returns a truthy
  value. If no predicate is given, `even?` is used by default."
  ([n] (make-stripes even? n))
  ([pred n]
   (mg/subdiv :cols n :out (mapv #(if (pred %) {}) (range n)))))

(def tree
  (let [branch (fn [[dir lpos]]
                 (mg/subdiv-inset
                  :dir :y :inset 0.05
                  :out {lpos (mg/subdiv dir 3 :out {1 nil}) 4 nil}))
        module (mg/subdiv-inset
                :dir :y :inset 0.4
                :out (mapv branch [[:cols 0] [:cols 1] [:slices 2] [:slices 3]]))]
    (mg/subdiv
     :rows 3
     :out [module
           (mg/subdiv
            :rows 3 :out {1 (mg/subdiv :cols 3 :out [nil {} nil])})
           module])))

(save-stl (mg/seed-box (a/aabb 1 0.5 1))

          (mg/subdiv :cols 3 :rows 3 :out [{} nil {}
                                           {} nil (mg/scale-edge :bc {:sym :y :scale 0.8})
                                           nil (mg/subdiv :cols 3 :out [nil {} nil
                                                                        ]) nil]
)


          "foo.stl" 1e6)


(defn save-stl [seed tree path max-depth]
  (with-open [o (io/output-stream path)]
    (->> (mg/generate-mesh seed tree max-depth)
         (g/tessellate)
         (mio/write-stl (mio/wrapped-output-stream o)))))

(mg/seed-box )

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
