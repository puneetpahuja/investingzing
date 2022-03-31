(ns investingzing.mf.routes
  (:require [investingzing.mf.handlers :as mf]
            [investingzing.middleware :as mw]
            ;; [investingzing.responses :as responses]
            ))

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
             :summary "Update schemes' details from API."
            ;;  :no-doc true
             }}]
     ["/update-nav-from-api/:code"  ;; use code 0 to update all
      {:get {:handler (mf/update-nav-from-api db)
             :summary "Update NAV values for a scheme from API."
             :parameters {:path {:code int?}}}}]
     ["/update-schemes-and-nav-from-navall"
      {:get {:handler (mf/update-schemes-and-nav-from-navall db)
             :summary "Updates schemes' details and NAV prices from API."}}]]))
