(ns spotify-analyzer.db
  (:require [next.jdbc :as jdbc]
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
