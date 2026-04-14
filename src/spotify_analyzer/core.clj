(ns spotify-analyzer.core
  (:require [clojure.string :as str]
            [integrant.core :as ig]
            [next.jdbc :as jdbc]
            [migratus.core :as migratus]
            [environ.core :refer [env]]
            [spotify-analyzer.db :as db]
            [spotify-analyzer.spotify :as spotify]
            [tea-time.core :as tt]))

(defn ->jdbc-url [url]
  (when url
    (cond-> url
      (not (str/starts-with? url "jdbc:")) (str/replace-first #"^" "jdbc:")
      (not (str/includes? url "prepareThreshold")) (str "?prepareThreshold=0"))))

(def config
  {::db        {}
   ::migrator  {:db (ig/ref ::db)}
   ::scheduler {:db (ig/ref ::db)}})

(defmethod ig/init-key ::db [_ _]
  (println "Connecting to database...")
  (let [jdbc-url (->jdbc-url (or (System/getProperty "database-url")
                                  (System/getenv "DATABASE_URL")))
        ds       (jdbc/get-datasource jdbc-url)]
    (jdbc/execute! ds ["SELECT 1"])
    (println "Database connection OK")
    ds))

(defmethod ig/halt-key! ::db [_ ds]
  (println "Shutting down database connection"))

(defmethod ig/init-key ::migrator [_ {:keys [db]}]
  (let [cfg {:store                :database
             :migration-dir        "migrations/"
             :migration-table-name "schema_migrations"
             :db                   {:datasource db}}]
    (println "Running migrations...")
    (migratus/migrate cfg)
    (println "Migrations OK")
    cfg))

(defmethod ig/init-key ::scheduler [_ {:keys [db]}]
  (tt/start!)
  (tt/every! (* 60 60) #(ingest! db))
  (println "Scheduler started"))

(defmethod ig/halt-key! ::scheduler [_ _]
  (tt/stop!)
  (println "Scheduler stopped"))

(defn ingest! [ds]
  (let [cursor    (db/get-cursor ds)
        after     (:ingestion_cursor/last_played_at cursor)
        token     (spotify/refresh-access-token)
        events    (spotify/recently-played token (when after {:after (.getTime after)}))
        sorted    (sort-by :played-at events)]
    (when (seq sorted)
      (db/insert-play-events! ds sorted)
      (db/upsert-cursor! ds (:played-at (last sorted))))))

(defn -main [& args]
  (let [system (ig/init config)]
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. #(ig/halt! system)))
    (println "System started")))
