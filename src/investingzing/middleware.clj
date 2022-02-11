(ns investingzing.middleware)

(def wrap-print-response
  {:name ::print-response
   :description "Middleware to print response"
   :wrap (fn [handler]
           (fn [req]
             (let [resp (handler req)]
               (println resp)
               resp)))})
