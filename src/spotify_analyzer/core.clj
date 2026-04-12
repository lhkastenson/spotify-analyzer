(ns spotify-analyzer.core
  (:require [clojure.string :as str]
            [integrant.core :as ig]
            [next.jdbc :as jdbc]
            [environ.core :refer [env]]))

(defn ->jdbc-url [url]
  (if (and url (not (str/starts-with? url "jdbc:")))
    (str "jdbc:" url)
    url))

(def config
  {::db {:jdbc-url (->jdbc-url (env :database-url))}})

(defmethod ig/init-key ::db [_ {:keys [jdbc-url]}]
  (println "Connecting to database...")
  (let [ds (jdbc/get-datasource jdbc-url)]
    (jdbc/execute! ds ["SELECT 1"])
    (println "Database connection OK")
    ds))

(defmethod ig/halt-key! ::db [_ ds]
  (println "Shutting down database connection"))

(defn -main [& args]
  (let [system (ig/init config)]
    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. #(ig/halt! system)))
    (println "System started")))
