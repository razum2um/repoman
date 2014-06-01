(ns repoman.korma)
; https://gist.github.com/jeroenvandijk/2e8a3d55d80707ce79e0

;*****************************************************
; Korma.core
;*****************************************************

(ns korma.core
  "Core querying and entity functions"
  (:use korma.core))

(defn upsert* [ent]
  "Create an empty upsert query. Ent can either be an entity defined by defentity,
  or a string of the table name."
  [ent]
  (if (:type ent)
    ent
    (let [q (empty-query ent)]
      (merge q {:type :upsert
                :values []
                :upsert-keys [(:pk ent)]
                :results :keys}))))

(defmacro upsert
  "Creates an upsert query, applies any modifying functions in the body and then
  executes it. `ent` is either a string or an entity created by defentity.

  ex: (upsert user s
  (values {:name \"chris\"})
  (set-upsert-keys :user_id))

  Results in the following upsert query.

  BEGIN WORK;
  LOCK TABLE rss_apps IN SHARE MODE;
  WITH new_values (store, id) as ( VALUES ('NL', '1') ),
  upsert as (
  UPDATE rss_apps m SET store = nv.store
  FROM new_values nv
  WHERE m.id = nv.id
  RETURNING m.*)
  INSERT INTO rss_apps (store, id)
  SELECT store, id
  FROM new_values
  WHERE NOT EXISTS (SELECT 1 FROM upsert up WHERE up.id = new_values.id);
  COMMIT WORK"
  [ent & body]
  `(let [query# (-> (upsert* ~ent)
                    ~@body)]
     (exec query#)))

(defn set-upsert-keys
  "Set the fields and values for an update query."
  [query ks]
  (merge query {:upsert-keys (if (vector? ks ) ks [ks]) }))

;;*****************************************************
;; korma.sql.engine
;;*****************************************************

(ns korma.sql.engine
  (:require [clojure.string :as string]
            [korma.sql.utils :as utils]
            [korma.config :as conf]
            [clojure.walk :as walk]))

; http://stackoverflow.com/questions/1109061/insert-on-duplicate-update-postgresql
(defn sql-upsert [query]
  (let [upsert-keys (:upsert-keys query)
        upsert-keys-str (map name upsert-keys)
        upsert-key-fn (fn [t1 t2] (clojure.string/join " AND " (map #(str t1 "." % " = " t2 "." %) upsert-keys-str)))
        ins-keys (keys (first (:values query)))
        upd-keys (map name (disj (set ins-keys) upsert-keys))
        upd-clause (clojure.string/join ", " (map #(str % " = nv." %) upd-keys))
        keys-clause (utils/comma-separated (map name ins-keys))
        ins-values (insert-values-clause ins-keys (:values query))
        values-clause (utils/comma-separated ins-values)
        table (:table query)
        neue-sql (str "LOCK TABLE " table " IN SHARE ROW EXCLUSIVE MODE;\n"
                      "WITH new_values " (utils/wrap keys-clause) " as ( VALUES " values-clause " ),\n"
                      "upsert as (\n"
                      "UPDATE " table " m SET " upd-clause "\n"
                      "FROM new_values nv\n"
                      "WHERE " (upsert-key-fn "m" "nv") "\n"
                      "RETURNING m.*"
                      ")\n"
                      "INSERT INTO " table " " (utils/wrap keys-clause) "\n"
                      "SELECT " keys-clause "\n"
                      "FROM new_values\n"
                      "WHERE NOT EXISTS (SELECT 1 FROM upsert up WHERE " (upsert-key-fn "up" "new_values") ");"
                      )]
    (assoc query :sql-str neue-sql)))

(defn ->sql [query]
  (bind-params
   (case (:type query)
     :upsert (-> query sql-upsert)
     :union (-> query sql-union sql-order)
     :union-all (-> query sql-union-all sql-order)
     :intersect (-> query sql-intersect sql-order)
     :select (-> query
                 sql-select
                 sql-joins
                 sql-where
                 sql-group
                 sql-having
                 sql-order
                 sql-limit-offset)
     :update (-> query
                 sql-update
                 sql-set
                 sql-where)
     :delete (-> query
                 sql-delete
                 sql-where)
     :insert (-> query
                 sql-insert))))

; Add upsert type
;; (defmethod ->sql :upsert [query]
;;   (bind-params
;;    (-> query
;;        (sql-upsert))))
