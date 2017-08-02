(ns clojure-test.core
  (:require [clj-http.client :as http])
  (:require [clojure.data.json :as json])
  (:require [capacitor.core :as influx])
  (:gen-class))

(defmacro forever [& body]
  `(while true ~@body))

(def database (influx/make-client {:db "orderbookBTCUSD" :host "bigdatadev"}))
(println "Connected to the database with a " (influx/ping database) "ms ping")
(influx/create-db database)

(def url "https://api.bitfinex.com/v1")
(def BTCUSD "BTCUSD")

(defn get-orderbook [symbol]
  (json/read-str (get (http/get (str url "/book/" symbol "?limit_bids=15&limit_asks=15&group=1")) :body))
  )

(defn get-points [orders side timestamp]
  (def as-point (fn [order]
                  {
                    :measurement "orderbook"
                    :tags {"exchange" "bitfinex" "index" (.indexOf orders order) "side" side}
                    :fields {"price" (get order "price") "amount" (get order "amount")}
                    :timestamp timestamp
                    }
                  ))
  (map as-point orders)
  )

(defn get-orders [orderbook]
  (def timestamp (System/currentTimeMillis))
  (println timestamp)
  (def bids (get-points (get orderbook "bids") "bid" timestamp))
  (def asks (get-points (get orderbook "asks") "ask" timestamp))
  (concat bids asks)
  )

(defn save [symbol]
  (let [orderbook (get-orderbook symbol)]
    (let [orders (get-orders orderbook)]
        (println (count orders))
        (influx/write-points database orders)
      )
    )
  )

(defn -main [& args]
  (forever (save BTCUSD) (Thread/sleep 1000))
  )
(-main)
