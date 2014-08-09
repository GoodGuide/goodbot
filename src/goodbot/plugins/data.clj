(ns goodbot.plugins.data
  "runs datalog queries against the datomic db"
  (:require [clojail.core :as clojail]
            [datomic.api :as datomic]
            [goodbot.db :as db]))

(def sandbox (clojail/sandbox clojail.testers/secure-tester :timeout 5000))

(defn handle-data [irc message]
  (def conn (db/get-conn irc))
  (def db-value (datomic/db conn))
  (def query (-> message
                 :text
                 clojail/safe-read))
  (str (datomic/q query db-value)))

(def plugin {:author "jneen"
             :doc {"data" "runs datalog queries against the datomic db"}
             :commands {"data" handle-data}})
