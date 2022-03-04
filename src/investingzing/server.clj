(ns investingzing.server
  (:require [investingzing.router :as router]
            ;; [reitit.ring :as ring]
            [ring.adapter.jetty :as jetty]
            [integrant.core :as ig]
            [environ.core :refer [env]])
  ;; (:import (com.zaxxer.hikari HikariDataSource))
  )

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
  [_ config]
  (println "\n Configured db")
  (:jdbc-url config))

(defmethod ig/halt-key! :server/jetty
  [_ jetty]
  (.stop jetty))

(defn -main [config-file]
  (let [config (-> config-file slurp ig/read-string)]
    (future
      (dotimes [_ 100000000]
        (Thread/sleep 300000)
        (slurp "http://api.investingzing.com/v1/mf/navall-get-date")))
    (-> config ig/prep ig/init)))
