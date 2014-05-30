(ns repoman.core
  (:gen-class))

(defn first-element [sequence default]
  (if (nil? sequence)
    default
    (first sequence)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
