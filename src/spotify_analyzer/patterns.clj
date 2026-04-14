(ns spotify-analyzer.patterns)

(defn frequency-score [events]
  (->> events
       (group-by :track-id)
       (map (fn [[_ plays]]
              (let [track (first plays)]
                {:track-id    (:track-id track)
                 :track-name  (:track-name track)
                 :artist-name {:artist-name track}
                 :play-count  (count plays)})))
       (sort-by :play-count >)))
