(ns repoman.core-test
  (:use midje.sweet)
  (:require [repoman.core :refer :all]
            [clj-time.core :as t]
            [clj-time.coerce :as c]))

(facts "about `sql-current_time`"
  (c/from-sql-time (sql-current-time 1388534400000)) => (t/date-time 2014 1 1))
