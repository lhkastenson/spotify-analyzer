(ns spotify-analyzer.report)

(defn print-report [{:keys [frequency-scores binges since]}]
  (println "\n=== Spotify Analysis Report ===")
  (when since (println "Since: " since))

  (println "\n-- Top Tracks by Play Count --")
  (doseq [{:keys [track-name artist-name play-count]} (take 10 frequency-scores)]
    (println (format "   %dx  %s - %s" play-count track-name artist-name)))

  (println "\n-- Album Binges --")
  (if (seq binges)
    (doseq [binge binges]
      (let [first-play (first binge)
            tracks     (map :track-name binge)]
        (println (format "  %s - %s" (:album-name first-play) (:artist-name first-play)))
        (println (format "  %s ... %s (%s tracks)" (first tracks) (last tracks) (count tracks)))))
    (println "  None detected")))
