(ns clojure-test.incanter
  (:use (incanter core stats charts io))
  (:gen-class))

(def data (read-dataset "/home/sirolf2009/git/rnn/data/ohlc-2017.csv"))

(view data)
