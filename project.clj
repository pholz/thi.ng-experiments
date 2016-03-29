(defproject phdemo "0.1.0-SNAPSHOT"
  :description "playing around with tin.ng morphogen"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [thi.ng/morphogen "0.2.0-SNAPSHOT"]
                 [thi.ng/geom "0.0.908"]
                 [thi.ng/common "0.3.1"]]
  :main ^:skip-aot phdemo.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
