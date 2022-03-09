(ns investingzing.mf.db
  (:require [next.jdbc.sql :as sql]))

(defn insert-schemes2 [db schemes-details]
  (map
   #(sql/insert-multi!
     db
     :mf_scheme
     [:code :name]
     (mapv
      (fn [scheme-detail]
        [(get scheme-detail "schemeCode") (get scheme-detail "schemeName")])
      %)
     {:suffix "ON CONFLICT (code) DO UPDATE SET \"name\" = EXCLUDED.name"})
   (partition 100 schemes-details)))

(defn insert-schemes [db schemes-details]
  (for [scheme-details schemes-details]
    (let [{:strs [schemeCode schemeName]} scheme-details]
      (sql/insert! db
                   :mf_scheme
                   {:code schemeCode :name schemeName}
                   {:suffix "ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name"}))))
