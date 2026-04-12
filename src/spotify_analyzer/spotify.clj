(ns spotify-analyzer.spotify
  (:require [hato.client :as http]))

(defn- prop [k]
  (or (System/getProperty k)
      (System/getenv (-> k (.replace "-" "_") .toUpperCase))))

(def token-url "https://accounts.spotify.com/api/token")
(def api-base-url "https://api.spotify.com/v1")

(defn refresh-access-token []
  (let [resp (http/post token-url
    {:form-params  {:grant_type    "refresh_token"
                    :refresh_token (prop "spotify-refresh-token")}
     :basic-auth   {:user (prop "spotify-client-id")
                    :pass (prop "spotify-client-secret")}
     :as           :json})]
    (get-in resp [:body :access_token])))

(defn- parse-item [item]
  (let [track  (:track item)
        album  (:album track)
        artist (first (:artists track))]
    {:track-id     (:id track)
     :track-name   (:name track)
     :track-number (:track_number track)
     :duration-ms  (:duration_ms track)
     :popularity   (:popularity track)
     :artist-id    (:id artist)
     :artist-name  (:name artist)
     :album-name   (:name album)
     :album-type   (:album_type album)
     :release-date (:release_date album)
     :played_at    (:played_at item)
     :context-type (get-in item [:context :type])
     :context-uri  (get-in item [:context :uri])}))

(defn recently-played [access-token & [{:keys [after]}]]
  (let [params (cond-> {:limit 50}
                 after (assoc :after after))
        resp   (http/get (str api-base-url "/me/player/recently-played")
                         {:oauth-token  access-token
                          :query-params params
                          :as           :json})]
    (map parse-item (get-in resp [:body :items]))))
