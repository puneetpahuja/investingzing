(ns investingzing.mf.db
  (:require [next.jdbc.sql :as sql]
            [clojure.tools.logging :as l]))

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

(defn insert-schemes [db schemes-details]
  (l/info "START update scheme details from MF API")
  (doseq [scheme-details schemes-details]
    (let [{:strs [schemeCode schemeName]} scheme-details]
      (sql/insert! db
                   :mf_scheme
                   {:code schemeCode :name schemeName}
                   {:suffix "ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name"})))
  (l/info "DONE update scheme details from MF API"))

(defn insert-schemes-and-nav [db details]
  (l/info "START update scheme and NAV details from NAVAll.txt")
  (doseq [detail (take 2 details)]
    (println detail)
    (sql/insert! db
                 :mf_scheme
                 (-> detail
                     (select-keys [:code :isin_dev_reinvestment :name]) ; :isin_div_payout_growth :name :house :category])
                     (update :code #(Integer/parseInt %)))
                 {:suffix "ON CONFLICT (code) DO UPDATE SET 
                           name = EXCLUDED.name, 
                           isin_dev_reinvestment = EXCLUDED.isin_dev_reinvestment, 
                           isin_div_payout_growth = EXCLUDED.isin_div_payout_growth, 
                           name = EXCLUDED.name, 
                           house = EXCLUDED.house, 
                           category = EXCLUDED.category"})
    (println 2)
    (sql/insert! db
                 :mf_nav
                 (-> detail
                     (select-keys [:code :date :nav])
                     (update :code #(Integer/parseInt %))
                     (update :nav #(Double/parseDouble %)))
                 {:suffix "ON CONFLICT (code, date, nav) DO NOTHING"}))
  (l/info "DONE update scheme and NAV details from NAVAll.txt"))
