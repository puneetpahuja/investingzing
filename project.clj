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
                 [com.zaxxer/HikariCP "5.0.1"]
                 ;; Use Logback as the main logging implementation:
                 [ch.qos.logback/logback-classic "1.2.11"]
                 [ch.qos.logback/logback-core "1.2.11"]
                 ;; Logback implements the SLF4J API:
                 [org.slf4j/slf4j-api "1.7.36"]
                  ;; Redirect Apache Commons Logging to Logback via the SLF4J API:
                 [org.slf4j/jcl-over-slf4j "1.7.36"]
                  ;; Redirect Log4j 1.x to Logback via the SLF4J API:
                 [org.slf4j/log4j-over-slf4j "1.7.36"]
                  ;; Redirect Log4j 2.x to Logback via the SLF4J API:
                 [org.apache.logging.log4j/log4j-to-slf4j "2.17.2"]
                  ;; Redirect OSGI LogService to Logback via the SLF4J API
                 [org.slf4j/osgi-over-slf4j "1.7.36"]
                  ;; Redirect java.util.logging to Logback via the SLF4J API.
                  ;; Requires installing the bridge handler, see README:
                 [org.slf4j/jul-to-slf4j "1.7.36"]]
  :exclusions [;; Exclude transitive dependencies on all other logging
               ;; implementations, including other SLF4J bridges.
               commons-logging
               log4j
               org.apache.logging.log4j/log4j
               org.slf4j/simple
               org.slf4j/slf4j-jcl
               org.slf4j/slf4j-nop
               org.slf4j/slf4j-log4j12
               org.slf4j/slf4j-log4j13]
  :repl-options {:init-ns investingzing.server}
  :profiles {:uberjar {:aot :all}
             :dev {:source-paths ["dev/src"]
                   :resource-paths ["dev/resources"]
                   :dependencies [[ring/ring-mock "0.4.0"]
                                  [integrant/repl "0.3.2"]]}}
  :uberjar-name "investingzing.jar"
  :main investingzing.server)
