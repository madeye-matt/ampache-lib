(defproject com.madeye.clojure.ampache/ampachedb "0.1.2"
  :description "A set of Clojure libraries for interacting with ampache music server through it's database"
  :url "https://github.com/madeye-matt/ampache-lib"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
    [org.clojure/clojure "1.5.1"]
    [korma "0.3.0-RC5"]
    [mysql/mysql-connector-java "5.1.25"] 
    [com.madeye.clojure.common/common "0.1.1"] 
  ]
  :main com.madeye.clojure.ampache.ampachedb)
