(ns investingzing.server
  (:require [environ.core :refer [env]]
            [integrant.core :as ig]
            [investingzing.router :as router] ;; [reitit.ring :as ring]
            [next.jdbc :as jdbc]
            [ring.adapter.jetty :as jetty]
            [next.jdbc.connection :as njc])
  (:import (com.zaxxer.hikari HikariDataSource)))

(defn app [envn]
  (router/routes envn))

(defmethod ig/prep-key :server/jetty
  [_ config]
  (if (env :port)
    (merge config {:port (Integer/parseInt (env :port))})
    config))

(defmethod ig/prep-key :db/postgres
  [_ config]
  (if (env :jdbc-database-url)
    (merge config {:jdbc-url (env :jdbc-database-url)})
    config))

(defmethod ig/init-key :server/jetty
  [_ {:keys [handler port]}]
  (println (str "\nServer running on port " port))
  (jetty/run-jetty handler {:port port :join? false}))

(defmethod ig/init-key :investingzing/app
  [_ config]
  (println "\nStarted app")
  (app config))

(defmethod ig/init-key :db/postgres
  [_ {:keys [jdbc-url]}]
  (println "\nConfigured db")
  (jdbc/with-options
    (njc/->pool HikariDataSource {:jdbcUrl jdbc-url})
    jdbc/snake-kebab-opts))

(defmethod ig/halt-key! :db/postgres
  [_ config]
  (.close ^HikariDataSource (:connectable config)))

(defmethod ig/halt-key! :server/jetty
  [_ jetty]
  (.stop jetty))

(defn -main [config-file]
  (let [config (-> config-file slurp ig/read-string)]
    (-> config ig/prep ig/init)))
