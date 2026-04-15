(ns spotify-analyzer.report
  (:require [clojure.string :as str]))

(defn- format-time [inst]
  (let [ldt (-> inst
                .toInstant
                (.atZone (java.time.ZoneId/systemDefault)))
        fmt (java.time.format.DateTimeFormatter/ofPattern "MMM d, h:mma")]
    (str/lower-case (.format fmt ldt))))

(defn print-report [{:keys [frequency-scores album-binges since artist-binges]}]
  (println "\n=== Spotify Analysis Report ===")
  (when since (println "Since: " since))

  (println "\n-- Top Tracks by Play Count --")
  (doseq [{:keys [track-name artist-name play-count]} (take 10 frequency-scores)]
    (println (format "   %dx  %s - %s" play-count track-name artist-name)))

  (println "\n-- Album Binges --")
  (if (seq album-binges)
    (doseq [binge album-binges]
      (let [first-play (first binge)
            tracks     (map :track-name binge)]
        (println (format "  %s - %s" (:album-name first-play) (:artist-name first-play)))
        (println (format "  %s ... %s (%s tracks)" (first tracks) (last tracks) (count tracks)))))
    (println "  None detected"))
  (println "\n-- Artist Binges --")
  (if (seq artist-binges)
    (doseq [binge artist-binges]
      (let [first-play (first binge)
            albums     (distinct (map :album-name binge))]
        (println (format "  %s" (:artist-name first-play)))
        (println (format "  %s (%s tracks across %s albums)"
                         (clojure.string/join ", " albums)
                         (count binge)
                         (count albums)))))
    (println "  None detected")))
