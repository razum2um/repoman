(defproject repoman "0.1.0-SNAPSHOT"
  :description "Helps you monitor GitHub repos"
  :url "https://github.com/razum2um/repoman"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :main ^:skip-aot repoman.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
