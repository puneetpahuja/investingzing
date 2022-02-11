(ns investingzing.router
  (:require [reitit.ring :as ring]
            [investingzing.mf.routes :as mf]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [muuntaja.core :as m]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.coercion.spec :as coercion-spec]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.middleware.exception :as exception]
            [reitit.dev.pretty :as pretty]
            [reitit.ring.spec :as rs]
            [reitit.ring.middleware.dev :as dev]))

(def swagger-docs
  ["/swagger.json"
   {:get
    {:no-doc true
     :swagger {:basePath "/"
               :info {:title "InvestingZing API Reference"
                      :description "API Details"
                      :version "1.0.0"}}
     :handler (swagger/create-swagger-handler)}}])

(def router-config
  {:validate  rs/validate
  ;;  :reitit.middleware/transform dev/print-request-diffs
   :exception pretty/exception
   :data      {:coercion   coercion-spec/coercion
               :muuntaja   m/instance
               :middleware [swagger/swagger-feature
                            muuntaja/format-middleware
                            exception/exception-middleware
                            coercion/coerce-request-middleware
                            coercion/coerce-response-middleware]}})

(defn routes [envn]
  (ring/ring-handler
   (ring/router
    [swagger-docs
     ["/v1"
      (mf/routes envn)]]
    router-config)
   (ring/routes
    (swagger-ui/create-swagger-ui-handler {:path "/"}))))
