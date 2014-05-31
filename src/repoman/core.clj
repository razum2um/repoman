(ns repoman.core
  (:require [clojure.pprint :refer :all])
  (:require [environ.core :refer [env]])
  (:require [korma.db :refer :all])
  (:require [korma.core :refer :all])
  (:require [clj-http.client :as client])
  (:require [cheshire.core :as json])
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


;;(get-counts username repo cookie)
;;(get-stats username repo cookie)
