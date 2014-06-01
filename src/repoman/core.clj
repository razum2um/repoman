(ns repoman.core
  (:use [repoman.korma])
  (:require [clojure.pprint :refer :all]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [environ.core :refer [env]]
            [korma.db :refer :all]
            [korma.core :refer :all]
            [clj-http.client :as client]
            [cheshire.core :as json])
  (:gen-class))



(def repo (env :repo))
(def username (env :username))
(def cookie (env :cookie))
(def db-creds (env :database))

(defdb db (postgres db-creds))

(defentity users)
(defentity repos
  (belongs-to users {:fk 'user_id}))
(defentity stats
  (belongs-to repos {:fk 'repo_id}))

(class (get (first (select users)) :updated_at))

(defn get-stats [user repo timestamp]
  (select stats
          (fields :stats.updated_at :total :unique)
          (with repos (fields) (where {:name repo})
                (with users (fields) (where {:name user})))))

;; (-> (select* stats)
;;     ;;(fields)
;;     (with repos (fields)
;;           (with users (fields))) (as-sql))

(defn sql-current-time [& args]
  "Current timestamp in SQL Timestamp format"
  (let [arg (first args)]
    (println-str arg)
    (c/to-sql-time
     (if (number? arg)
       (c/from-long arg)
       (t/now)))))


(defn fetch-stats [user repo cookie]
  (client/get
   (format "https://github.com/%s/%s/graphs/traffic-data" user repo)
   {:cookies { "user_session" { :value cookie }}}
   :as :json))

(defn get-counts [user repo cookie]
  (json/parse-string
   (get
    (fetch-stats user repo cookie)
    :body)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  (pprint (get-counts username repo cookie))
  (pprint (get-stats username repo cookie)))

(defn test [&]
  (dry-run (upsert stats
                  (values {:id 90 :total 99 :taken_at
                           (sql-current-time 893462400000)}))))

(defn test1 [&]
  (dry-run (insert stats
                  (values {:total 99 :taken_at
                           (sql-current-time 893462400000)}))))
;;(get-counts username repo cookie)
;;(get-stats username repo cookie)
;; (upsert stats (values {:total 45 :unique 15}))

;; (insert stats (values {:total 99 :unique 99 :taken_at (sql-current-time(893462400000))}))
