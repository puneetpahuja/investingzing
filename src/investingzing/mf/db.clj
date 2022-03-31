(ns investingzing.mf.db
  (:require [next.jdbc.sql :as sql]
            [clojure.tools.logging :as l]
            [java-time :as t]
            [next.jdbc.types :refer [as-other]]))

;; (defn insert-schemes2 [db schemes-details]
;;   (map
;;    #(sql/insert-multi!
;;      db
;;      :mf_scheme
;;      [:code :name]
;;      (mapv
;;       (fn [scheme-detail]
;;         [(get scheme-detail "schemeCode") (get scheme-detail "schemeName")])
;;       %)
;;      {:suffix "ON CONFLICT (code) DO UPDATE SET \"name\" = EXCLUDED.name"})
;;    (partition 100 schemes-details)))


;; `on conflict update` updates only the columns provided but leaves the older ones intact. so its not a whole row update, its a column-by-column update.

(defn upsert-schemes-from-api [db schemes-details]
  (l/info "START update scheme details from MF API")
  (doseq [scheme-details schemes-details]
    (let [{:strs [schemeCode schemeName]} scheme-details]
      (sql/insert! db
                   :mf_scheme
                   {:code schemeCode
                    :name schemeName
                    :source (as-other "api_less")}
                   {:suffix "ON CONFLICT (code) DO UPDATE SET 
                             name = EXCLUDED.name
                             WHERE 
                             mf_scheme.source = 'api_less' AND
                             mf_scheme.name <> EXCLUDED.name"})))
  (l/info "DONE update scheme details from MF API"))

(defn upsert-schemes-and-nav-from-navall [db details]
  (l/info "START update scheme and NAV details from NAVAll.txt")
  (doseq [detail details]
    (sql/insert! db
                 :mf_scheme
                 (-> detail
                     (select-keys [:code :isin_div_reinvestment :isin_div_payout_growth :name :house :type :category])
                     (update :code #(Integer/parseInt %))
                     (assoc :source (as-other "navall")))
                 {:suffix "ON CONFLICT (code) DO UPDATE SET 
                           isin_div_reinvestment = EXCLUDED.isin_div_reinvestment, 
                           isin_div_payout_growth = EXCLUDED.isin_div_payout_growth, 
                           name = EXCLUDED.name, 
                           house = EXCLUDED.house,
                           type = EXCLUDED.type,
                           category = EXCLUDED.category,
                           source = EXCLUDED.source
                           WHERE 
                           mf_scheme.source <> EXCLUDED.source OR
                           mf_scheme.name <> EXCLUDED.name OR
                           mf_scheme.house <> EXCLUDED.house"})
    (sql/insert! db
                 :mf_nav
                 (-> detail
                     (select-keys [:code :date :nav])
                     (update :code #(Integer/parseInt %))
                     (update :nav #(Double/parseDouble %))
                     (update :date (partial t/local-date (t/formatter "dd-MMM-yyyy"))))
                 {:suffix "ON CONFLICT (code, date, nav) DO NOTHING"}))
  (l/info "DONE update scheme and NAV details from NAVAll.txt"))

(defn upsert-nav-from-api [db details]
  (l/info "START update nav from MF API")
  (let [{:strs [meta data]} details
        {:strs [fund_house scheme_type scheme_category scheme_code scheme_name]} meta]
    (sql/insert! db
                 :mf_scheme
                 {:house fund_house
                  :type scheme_type
                  :category scheme_category
                  :code scheme_code
                  :name scheme_name
                  :source (as-other "api_more")}
                 {:suffix "ON CONFLICT (code) DO UPDATE SET 
                           name = EXCLUDED.name, 
                           house = EXCLUDED.house, 
                           category = EXCLUDED.category,
                           type = EXCLUDED.type,
                           source = EXCLUDED.source
                           WHERE 
                           mf_scheme.source = 'api_less' OR
                           (mf_scheme.source = 'api_more' AND
                             (mf_scheme.name <> EXCLUDED.name OR
                              mf_scheme.house <> EXCLUDED.house))"})
    (doseq [{:strs [date nav]} data]
      (sql/insert! db
                   :mf_nav
                   {:code scheme_code
                    :date (t/local-date (t/formatter "dd-MM-yyyy") date)
                    :nav (Double/parseDouble nav)}
                   {:suffix "ON CONFLICT (code, date, nav) DO NOTHING"})))
  (l/info "DONE update nav from MF API"))
