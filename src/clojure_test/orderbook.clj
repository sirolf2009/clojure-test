(ns clojure-test.core
  (:require [clj-http.client :as http])
  (:require [clojure.data.json :as json])
  (:require [capacitor.core :as influx])
  (:use (incanter core stats charts io))
  (:gen-class))

(def database (influx/make-client {:db "orderbookBTCUSD" :host "bigdatadev"}))

(defn get-orderbook []
  (let [response (influx/db-query database (str "SELECT * FROM orderbookBTCUSD.autogen.orderbook WHERE exchange='bitfinex'"))]
    (let [series (map (fn [series] (get series :series)) (get response :results))]
      (flatten series)
      )))

;; (load "data")

(def series (map (fn [x] {
                           :date (nth x 0)
                           :amount (nth x 1)
                           :exchange (nth x 2)
                           :index (nth x 3)
                           :price (nth x 4)
                           :side (nth x 5)
                           }) (get (first (get-orderbook)) :values)))
series
;; ({:date "2017-08-02T07:50:50.534Z", :amount "5.7", :exchange "bitfinex", :index "9", :price "2720.1", :side "bid"} {:date "2017-08-02T07:50:50.534Z", :amount "1.384", :exchange "bitfinex", :index "8", :price "2720.3", :side "bid"})

(def series (list {:date "2017-08-02T07:50:50.534Z", :amount "5.7", :exchange "bitfinex", :index "9", :price "2720.1", :side "bid"} {:date "2017-08-02T07:50:50.534Z", :amount "1.384", :exchange "bitfinex", :index "8", :price "2720.3", :side "bid"} {:date "2017-08-02T07:50:51.534Z", :amount "5.7", :exchange "bitfinex", :index "9", :price "2720.1", :side "bid"} {:date "2017-08-02T07:50:51.534Z", :amount "1.384", :exchange "bitfinex", :index "8", :price "2720.3", :side "bid"}))

(def series-by-time (partition-by #(get % :date) series))
series-by-time

(view (map (fn [orderbook]
       (def by-side (group-by :side orderbook))
       by-side
       (def bids (get by-side "bid"))
       (def asks (get by-side "ask"))
       (def date (get (first bids) :date))
       (println (first bids))
       (def bidswide (mapcat (fn [x] [(get x :price) (get x :amount)]) (sort-by :index bids)))
       (def askswide (mapcat (fn [x] [(get x :price) (get x :amount)]) (sort-by :index asks)))
       (concat [date] bidswide askswide)
       ) series-by-time))
