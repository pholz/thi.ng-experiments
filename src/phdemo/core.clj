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

          (mg/subdiv :cols 8 :rows 8 :out [{} nil {}
                                           {} nil (mg/scale-edge :bc {:sym :z :scale 0.8
                                                                      }
                                                                 :out [(mg/scale-edge :ad {:sym :y :scale 0.8})])
                                           nil (mg/subdiv :cols 3 :out [nil {} nil
                                                                        ]) nil]
)


          "foo.stl" 1e6)


(save-stl (mg/seed-box (a/aabb 1 1 0.4))

          (mg/subdiv :cols 8 :rows 8 :out (take 64 (repeatedly #(if (> (rand) 0.5)

                                                                  (let [scalef (rand)] (mg/scale-edge :bc :y :scale scalef
                                                                                                      :out [(mg/scale-edge :bf :x :scale scalef
                                                                                                                           :out [(mg/subdiv-inset :dir :z :inset 0.4 :out )])]))
                                                                  nil)))
)


          "foo2.stl" 1e6)

(defn extruder [sc it mx]
  (if (> it mx)
    {}
    (mg/extrude :dir :b :len 0.02 :out [(mg/scale-edge :bc :x :scale sc :out [(mg/subdiv :slices 2 :out [(extruder (Math/sin (* 50. sc)) (inc it) mx)
                                                                                                         {}])])])))

(defn scale-two-axes [ax1 s1 ax2 s2 sc ou]
  (mg/scale-edge ax1 s1 :scale sc :out [(mg/scale-edge ax2 s2 :scale sc :out ou)]))

(save-stl (mg/seed-box (a/aabb 1 1 0.8))

          (mg/subdiv :cols 8 :rows 8 :out (take 64 (repeatedly #(if (> (rand) 0.5)
                                                                  (scale-two-axes :bf :x :bc :y 0.1
                                                                                  [(scale-two-axes :ad :y :ae :x 0.7
                                                                                                    [(let [scalef (rand)]
                                                                                                       (let [d8 (take 8 (repeatedly rand))]
                                                                                                         (mg/subdiv :slices 8
                                                                                                                    :out (map (fn [i] (let [ld (nth d8 i)
                                                                                                                                           ud (nth d8 (+ 1 i))]
                                                                                                                                       (scale-two-axes :bc :y :bf :x ud [(scale-two-axes :ad :y :ae :x ld [{}])])
                                                                                                                                       #_(mg/scale-edge :bc :y :scale ud :out [(mg/scale-edge :ad :y :scale ld)])))
                                                                                                                              (range 7)))))])])
                                                                  nil)))
)


          "foo3.stl" 1e6)

(defn rotate-face
  [side angle & {:keys [out]}]
  {:op :rotate-face
   :args {:side side :angle angle}
   :out (mg/operator-output 1 out false)})

(defmethod mg/operator [BoxNode :rotate-face]
  [{[a b c d e f g h] :points :as node}
   {{:keys [side angle]} :args :as op}]
  (let [tfn #(let [ctr (g/div (-> b (g/+ c) (g/+ f) (g/+ g)) 4.0)]
               (-> % (g/- ctr) (g/rotate-z angle) (g/+ ctr))) #_(g/+ % (v/vec3 (Math/cos angle) (Math/sin angle) 0))]
    [(BoxNode.
      (case side
        :f [a (tfn b) (tfn c) d e (tfn f) (tfn g) h]
        :b [(tfn a) b c (tfn d) (tfn e) f g (tfn h)]
        :w [(tfn a) (tfn b) c d (tfn e) (tfn f) g h]
        :e [a b (tfn c) (tfn d) e f (tfn g) (tfn h)]
        :s [(tfn a) (tfn b) (tfn c) (tfn d) e f g h]
        :n [a b c d (tfn e) (tfn f) (tfn g) (tfn h)]

        [a b c d e f g h])
      node op (inc (mg/tree-depth node)))]))

(save-stl (mg/seed-box (a/aabb 1 1 0.8))
          (mg/subdiv :cols 3 :rows 3 :empty? true :out {4 (mg/extrude :len 4 :out [(mg/subdiv :rows 4 :out [{} {} {} (mg/extrude :len 3 :dir :w)])])
                                                        5 (mg/extrude :len 3 :out [(rotate-face :f (/ Math/PI 3.0))])})



          "foo4.stl" 1e6)

(take 64 (repeat {}))

(/ Math/PI 3.0)

(Math/cos Math/PI)

(mapv)

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
