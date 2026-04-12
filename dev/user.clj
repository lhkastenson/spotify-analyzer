(ns user
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn load-dotenv! []
  (when-let [f (io/file ".env")]
    (when (.exists f)
      (doseq [line (str/split-lines (slurp f))
              :when (and (seq line)
                         (not (str/starts-with? line "#"))
                         (str/includes? line "="))]
        (let [[k v] (str/split line #"=" 2)]
          (System/setProperty (-> k str/trim str/lower-case (str/replace "_" "-"))
                              (str/trim v)))))))

(load-dotenv!)
