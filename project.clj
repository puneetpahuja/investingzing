(defproject investingzing "0.1.0-SNAPSHOT"
  :description "Investingzing"
  :url "https://api.investingzing.com"
  :min-lein-version "2.0.0"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [clojure.java-time "0.3.3"]
                 [ring "1.9.5"]
                 [integrant "0.8.0"]
                 [environ "1.2.0"]
                 [metosin/reitit "0.5.15"]
                 [com.github.seancorfield/next.jdbc "1.2.772"]
                 [org.postgresql/postgresql "42.3.2"]
                 [clj-http "3.12.3"]
                 [ovotech/ring-jwt "2.3.0"]
                 [camel-snake-kebab "0.4.2"]
                 [cheshire "5.10.2"]
                 [org.clojure/tools.logging "1.2.4"]
                 [com.zaxxer/HikariCP "5.0.1"]]
  :repl-options {:init-ns investingzing.server}
  :profiles {:uberjar {:aot :all}
             :dev {:source-paths ["dev/src"]
                   :resource-paths ["dev/resources"]
                   :dependencies [[ring/ring-mock "0.4.0"]
                                  [integrant/repl "0.3.2"]]}}
  :uberjar-name "investingzing.jar"
  :main investingzing.server)
