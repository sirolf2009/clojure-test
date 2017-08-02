(ns clojure-test.core
  (:require [clj-http.client :as http])
  (:require [clojure.data.json :as json])
  (:require [capacitor.core :as influx])
  (:use (incanter core stats charts io))
  (:gen-class))

(def database (influx/make-client {:db "orderbookBTCUSD" :host "database"}))

(defn get-orderbook []
  (let [response (influx/db-query database (str "SELECT * FROM orderbookBTCUSD.autogen.orderbook WHERE time > now() - 5m AND exchange='bitfinex'"))]
    (let [series (map (fn [series] (get series :series)) (get response :results))]
      (flatten series)
      )))

(def series (first (get-orderbook)))
(view (get series :values))

(def series-by-time (vec (partition-by (fn [row] (first row)) (get series :values))))
series-by-time

(view (map (fn [orderbook]
       (def by-side (group-by (fn [row] (nth row 5)) orderbook))
       by-side
       (def bids (get by-side "bid"))
       (def asks (get by-side "ask"))
       (def date (first (first bids)))
       (def get-values (fn [row] {:index (nth row 3) :price (nth row 4) :amount (nth row 1)}))
       (def bidswide (mapcat (fn [x] [(get x :price) (get x :amount)]) (sort-by :index (map get-values bids))))
       (def askswide (mapcat (fn [x] [(get x :price) (get x :amount)]) (sort-by :index (map get-values asks))))
       (concat [date] bidswide askswide)
       ) series-by-time))
