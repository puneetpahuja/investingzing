(ns investingzing.mf.handlers
  (:require [ring.util.response :as rr]
            [java-time :as t]
            [clojure.string :as s]
            [clj-http.client :as client]
            [cheshire.core :as json]
            [investingzing.mf.db :as db]))

(defn get-dates!
  []
  (let [today (t/local-date (t/zone-id "Asia/Calcutta"))
        three-days (->> today
                        (iterate #(t/minus % (t/days 1)))
                        (take 3))]
    (map (partial t/format "dd-MMM-yyyy") three-days)))

(defn navall-get-date []
  (fn [_req]
    (let [navall-str (slurp "https://www.amfiindia.com/spages/NAVAll.txt")
          navall-lines (s/split-lines navall-str)
          first-line-with-date (get navall-lines 6)
          dates (get-dates!)
          update-fn (fn [amap date]
                      (let [number-of-occur (->> navall-str
                                                 (re-seq (re-pattern date))
                                                 count)]
                        (if (pos? number-of-occur)
                          (assoc amap date number-of-occur)
                          amap)))
          result (reduce update-fn {} dates)]
      (-> result
          (assoc
           :time (->> (t/zone-id "Asia/Calcutta")
                      t/local-date-time
                      (t/format "dd-MMM hh:mma"))
           :date-string first-line-with-date)
          rr/response))))

(defn get-json-body [url]
  (->> url
       client/get
       :body
       json/decode))

;; should add 33948 rows
(defn update-schemes-from-api [db]
  (fn [_req]
    (let [schemes-details (get-json-body "https://api.mfapi.in/mf")]
      (future (db/insert-schemes db schemes-details))
      (rr/response {:database "updating"}))))

(def fields-map
  {"Scheme Code" :code
   "ISIN Div Payout/ ISIN Growth" :isin_div_payout_growth
   "ISIN Div Reinvestment" :isin_dev_reinvestment
   "Scheme Name" :name
   "Net Asset Value" :nav
   "Date" :date})

(defn parse-data [field-list [fund-category fund-house last-updated data] navall-line]
  (let [splitted-navall-line (s/split navall-line #";")]
    (if (< (count splitted-navall-line) (count field-list))
      (cond
        (false? last-updated) [fund-category navall-line true data]
        :else [fund-house navall-line false data])
      (let [datum
            (-> (zipmap (map (partial get fields-map) field-list) splitted-navall-line)
                (assoc :category fund-category :house fund-house))]
        [fund-category fund-house false (conj data datum)]))))

(defn update-schemes-and-nav-from-navall [db]
  (fn [_req]
    (let [navall-lines (->> (slurp "https://www.amfiindia.com/spages/NAVAll.txt")
                            (s/split-lines)
                            (remove s/blank?))
          fields (-> navall-lines first (s/split #";"))
          [_fund-category _fund-house _last-updated data] (reduce (partial parse-data fields)
                                                                  [nil nil false []] (rest navall-lines))]
      (future (db/insert-schemes-and-nav db data))
      (rr/response {:database "updating"}))))
