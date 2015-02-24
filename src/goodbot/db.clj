(ns goodbot.db
  "data functions"
  (:require [datomic.api :as d :refer [db q]]))

(defn hello [] (println "hello"))

(defn get-conn [irc]
  (def uri (:datomic-uri @irc))
  (d/connect uri))

(defn transact [irc q]
  (println q)
  (def conn (get-conn irc))
  @(d/transact conn q))

(defn resource [name] (clojure.java.io/resource name))

(defn load-datoms-from-resource [conn resource]
  (with-open [r (-> resource
                    clojure.java.io/reader
                    java.io.PushbackReader.)]
    (doseq [datoms (clojure.edn/read
                     {:readers *data-readers*}
                     r)]
      @(d/transact conn [datoms]))))

(defn start [irc]
  (d/create-database (:datomic-uri @irc))
  (def conn (get-conn irc))
  (doseq [plugin (:plugins @irc)]
    (when-let [schema-name (:schema plugin)]
      (def resource-name (str "schemas/" schema-name))
      (if-let [resource (clojure.java.io/resource resource-name)]
        (do
          (println "loading schema" resource-name)
          (load-datoms-from-resource conn resource))
        (println "schema" resource-name "is missing, skipping.")))))
