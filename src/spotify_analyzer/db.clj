(ns spotify-analyzer.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]))

(defn insert-play-events! [ds events]
  (let [rows (map (fn [e]
                    {:track_id     (:track-id e)
                     :track_name   (:track-name e)
                     :track_number (:track-number e)
                     :duration_ms  (:duration-ms e)
                     :popularity   (:popularity e)
                     :artist_id    (:artist-id e)
                     :artist_name  (:artist-name e)
                     :album_id     (:album-id e)
                     :album_name   (:album-name e)
                     :album_type   (:album-type e)
                     :album_tracks (:album-tracks e)
                     :release_date (:release-date e)
                     :played_at    (:played-at e)
                     :context_type (:context-type e)
                     :context_uri  (:context-uri e)})
                  events)
        query (-> (h/insert-into :play_events)
                  (h/values rows)
                  (h/on-conflict :played_at :track_id)
                  (h/do-nothing)
                  (sql/format))]
    (jdbc/execute! ds query)))

(defn get-play-events [ds & [{:keys [since until]}]]
  (cond
    (and since until)
    (jdbc/execute! ds ["SELECT * FROM play_events WHERE played_at > ? AND played_at <= ?" since until]
                   {:builder-fn rs/as-unqualified-kebab-maps})
    since
    (jdbc/execute! ds ["SELECT * FROM play_events WHERE played_at > ?" since]
                   {:builder-fn rs/as-unqualified-kebab-maps})
    :else
    (jdbc/execute! ds ["SELECT * FROM play_events"]
                   {:builder-fn rs/as-unqualified-kebab-maps})))

(defn get-cursor [ds]
  (jdbc/execute-one! ds ["SELECT last_played_at FROM ingestion_cursor WHERE id= 1"]))

(defn upsert-cursor! [ds last-played-at]
  (let [query (-> (h/insert-into :ingestion_cursor)
                  (h/values [{:id             1
                              :last_played_at last-played-at}])
                  (h/on-conflict :id)
                  (h/do-update-set :last_played_at)
                  (sql/format))]
    (jdbc/execute! ds query)))
