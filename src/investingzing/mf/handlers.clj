(ns investingzing.mf.handlers
  (:require [ring.util.response :as rr]
            [java-time :as t]
            [clojure.string :as s]))

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
                      (assoc amap
                             date (->> navall-str
                                       (re-find (re-pattern date))
                                       boolean)))
          result (reduce update-fn {} dates)]
      (-> result
          (assoc
           :time (->> (t/zone-id "Asia/Calcutta")
                      t/local-date-time
                      (t/format "dd-MMM hh:mma"))
           :date-string first-line-with-date)
          rr/response))))
