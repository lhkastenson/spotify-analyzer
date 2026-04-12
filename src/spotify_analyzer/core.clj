(ns spotify-analyzer.core
  (:require [clojure.string :as str]
            [integrant.core :as ig]
            [next.jdbc :as jdbc]
            [migratus.core :as migratus]
            [environ.core :refer [env]]))

(defn ->jdbc-url [url]
  (when url
    (cond-> url
      (not (str/starts-with? url "jdbc:")) (str/replace-first #"^" "jdbc:")
      (not (str/includes? url "prepareThreshold")) (str "?prepareThreshold=0"))))

(def config
  {::db        {:jdbc-url (->jdbc-url (env :database-url))}
   ::migrator  {:db (ig/ref ::db)}})

(defmethod ig/init-key ::db [_ {:keys [jdbc-url]}]
  (println "Connecting to database...")
  (let [ds (jdbc/get-datasource jdbc-url)]
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

(defn -main [& args]
  (let [system (ig/init config)]
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. #(ig/halt! system)))
    (println "System started")))
