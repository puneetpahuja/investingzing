(ns investingzing.mf.handlers
  (:require [cheshire.core :as json]
            [clj-http.client :as client]
            [clojure.string :as s]
            [clojure.tools.logging :as l]
            [investingzing.mf.db :as db]
            [java-time :as t]
            [ring.util.response :as rr]))

(defn navall-get-date []
  (fn [_req]
    (let [navall-str (slurp "https://www.amfiindia.com/spages/NAVAll.txt")
          navall-lines (s/split-lines navall-str)
          first-line-with-date (get navall-lines 6)
          get-dates! (fn []
                       (let [today (t/local-date (t/zone-id "Asia/Calcutta"))
                             three-days (->> today
                                             (iterate #(t/minus % (t/days 1)))
                                             (take 3))]
                         (map (partial t/format "dd-MMM-yyyy") three-days)))
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

(defn future-with-exceptions [f & ops]
  (future
    (try (apply f ops)
         (catch Exception e
           (l/error e)))))

(defn get-json-body [url]
  (->> url
       client/get
       :body
       json/decode))

(defn update-schemes-from-api [db]
  (fn [_req]
    (let [schemes-details (get-json-body "https://api.mfapi.in/mf")]
      (future-with-exceptions db/upsert-schemes-from-api db schemes-details)
      (rr/response {:database "updating"}))))

(defn update-schemes-and-nav-from-navall [db]
  (fn [_req]
    (let [navall-lines (->> (slurp "https://www.amfiindia.com/spages/NAVAll.txt")
                            (s/split-lines)
                            (remove s/blank?))
          fields (-> navall-lines first (s/split #";"))
          parse-data
          (fn [field-list [fund-type-and-category fund-house last-updated data] navall-line]
            (let [splitted-navall-line (s/split navall-line #";")]
              (if (< (count splitted-navall-line) (count field-list))
                (cond
                  (false? last-updated) [fund-type-and-category navall-line true data]
                  :else [fund-house navall-line false data])
                (let [fields-map {"Scheme Code" :code
                                  "ISIN Div Payout/ ISIN Growth" :isin_div_payout_growth
                                  "ISIN Div Reinvestment" :isin_div_reinvestment
                                  "Scheme Name" :name
                                  "Net Asset Value" :nav
                                  "Date" :date}
                      split (fn [fund-type-and-category]
                              (as-> fund-type-and-category x
                                (butlast x)
                                (apply str x)
                                (s/split x #"\(")
                                (map s/trim x)))
                      [type category] (split fund-type-and-category)
                      datum
                      (-> (zipmap (map (partial get fields-map) field-list) splitted-navall-line)
                          (assoc :category category :type type :house fund-house))]
                  [fund-type-and-category fund-house false (conj data datum)]))))
          [_fund-category _fund-house _last-updated data] (reduce (partial parse-data fields)
                                                                  [nil nil false []] (rest navall-lines))]
      (future-with-exceptions db/upsert-schemes-and-nav-from-navall db data)
      (rr/response {:database "updating"}))))

(defn update-nav-from-api [db]
  (fn [req]
    (let [code (-> req :path-params :code Integer/parseInt)
          schemes (if (zero? code)
                    (->> (get-json-body "https://api.mfapi.in/mf")
                         (map #(get % "schemeCode"))
                         set)
                    [code])
          data (map #(get-json-body (str "https://api.mfapi.in/mf/" %)) schemes)]
      (doseq [nav-details data]
        (future-with-exceptions db/upsert-nav-from-api db nav-details)))
    (rr/response {:database "updating"})))
