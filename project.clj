(defproject repoman "0.1.0-SNAPSHOT"
  :description "Helps you monitor GitHub repos"
  :url "https://github.com/razum2um/repoman"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [cheshire "5.3.1"]
                 [korma "0.3.1"]
                 [environ "0.5.0"]
                 [postgresql/postgresql "9.1-901.jdbc4"]
                 [clj-http "0.9.2"]]
  :main ^:skip-aot repoman.core
  :target-path "target/%s"
  :profiles {
             :dev {
                   :plugins [[lein-midje "3.1.3"]]
                   :dependencies [[midje "1.5.1"]]}
             :uberjar {:aot :all}})
