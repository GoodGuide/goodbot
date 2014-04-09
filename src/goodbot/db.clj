(ns goodbot.db
  "data functions"
  (:require [datomic.api :as d :refer [db q]]))

(defn hello [] (println "hello"))

(defn get-conn [irc]
  (def uri (:datomic-uri @irc))
  (d/connect uri))

(defn resource [name] (clojure.java.io/resource name))

(defn load-datoms-from-resource [conn resource]
  (with-open [r (-> resource
                    clojure.java.io/reader
                    java.io.PushbackReader.)]
    (doseq [datoms (clojure.edn/read
                             {:readers *data-readers*}
                             r)]
      (d/transact conn [datoms]))))

(defn start [irc]
  (d/create-database (:datomic-uri @irc))
  (def conn (get-conn irc))
  (println "*********")
  (println "plugins: " (:plugins @irc))
  (doseq [plugin (:plugins @irc)]
    (def resource-name (str "schemas/" (:command plugin) ".edn"))
    (println "resource-name: " resource-name)
    (println "resource: " (clojure.java.io/resource resource-name))
    (when-let [resource (clojure.java.io/resource resource-name)]
      (println "loading schema " resource-name)
      (load-datoms-from-resource conn resource))))
