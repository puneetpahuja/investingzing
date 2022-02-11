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
              :summary   "See if we have NAVAll.txt for the given date"}}]]))
