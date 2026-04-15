(ns spotify-analyzer.patterns)

(defn frequency-score [events]
  (->> events
       (group-by :track-id)
       (map (fn [[_ plays]]
              (let [track (first plays)]
                {:track-id    (:track-id track)
                 :track-name  (:track-name track)
                 :artist-name (:artist-name track)
                 :play-count  (count plays)})))
       (sort-by :play-count >)))

(defn cluster-sessions [events]
  (let [sorted (sort-by :played-at events)]
    (reduce (fn [sessions event]
              (let [last-session (last sessions)
                    last-play    (last last-session)
                    gap          (when last-play
                                   (- (.getTime (:played-at event))
                                      (.getTime (:played-at last-play))))]
                (if (or (nil? gap)
                        (> gap (* 30 60 1000)))
                  (conj sessions [event])
                  (update sessions (dec (count sessions)) conj event))))
            []
            sorted)))

(defn catalog-binge? [session]
  (let [album-groups (->> session
                          (group-by :album-id)
                          (filter (fn [[album-id plays]]
                                    (and album-id
                                         (> (count plays) 1)))))]
    (mapcat (fn [[_ plays]]
              (let [sorted (sort-by :track-number plays)]
                (when (apply < (map :track-number sorted)) sorted)))
            album-groups)))

(defn detect-album-binges [sessions]
  (->> sessions
       (map catalog-binge?)
       (filter seq)))

(defn artist-binge? [session]
  (->> session
       (partition-by :artist-id)
       (filter (fn [run]
                 (and (>= (count run) 3)
                      (> (count (distinct (map :album-id run))) 1))))))

(defn detect-artist-binges [sessions]
  (->> sessions
       (mapcat artist-binge?)))
