(ns investingzing.mf.routes
  (:require [investingzing.mf.handlers :as mf]
            [investingzing.middleware :as mw]
            [investingzing.responses :as responses]))

(defn routes [envn]
  (let [db (:jdbc-url envn)]
    ["/mf" {:swagger {:tags ["Mutual Funds"]}}
     ["/navall-get-date"
      {:get  {:handler   (mf/navall-get-date)
              :middleware [[mw/wrap-print-response]]
              ;; :responses {200 {:body nil?}}
              :summary   "See how many schemes have NAVs for today and past two days."}}]
     ["/update-schemes-from-api"
      {:get {:handler (mf/update-schemes-from-api db)
             :summary "Update schemes' details from API."}}]
     ["/update-schemes-and-nav-from-navall"
      {:get {:handler (mf/update-schemes-and-nav-from-navall db)
             :summary "Updates schemes' details and NAV prices from API."}}]]))
