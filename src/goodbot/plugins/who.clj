(ns goodbot.plugins.who
  "Asks who people are"
  (:require [goodbot.db :as db]
            [goodbot.parse :refer [extract-word]]
            [datomic.api :as datomic]))

(def who-is-query '[:find ?e :in $ ?nick :where [?e :who.entry/nick ?nick]])

(defmulti handle-who
  (fn [_ _ c _] c))

(defmethod handle-who "add" [irc message _ rest]
  (def conn (db/get-conn irc))
  (when-let [[who-nick desc] (extract-word {:text rest})]
    (println "add-who" message)
    @(datomic/transact conn [{:db/id (datomic/tempid :db.part/who)
                              :who.entry/nick who-nick
                              :who.entry/submitter (:nick message)
                              :who.entry/desc desc}])
    (str "added " who-nick " as " desc " (by " (:nick message) ")")))

(defn find-entry [db nick]
  (when-let [[id] (->> db
                       (datomic/q who-is-query db nick)
                       first)]
    (datomic/entity db id)))

(defmethod handle-who "is" [irc message _ rest]
  (def conn (db/get-conn irc))
  (def db (datomic/db conn))
  (when-let [[who-nick _] (extract-word {:text rest})]
    (if-let [entry (find-entry db who-nick)]
      (str who-nick " is " (:who.entry/desc entry))
      (str "no entry for " who-nick "."))))

(defmethod handle-who "added" [irc message _ rest]
  (def conn (db/get-conn irc))
  (def db (datomic/db conn))
  (when-let [[who-nick _] (extract-word {:text rest})]
    (if-let [entry (find-entry db who-nick)]
      (str "entry for " who-nick " was added by " (:who.entry/submitter entry))
      (str "no entry for " who-nick "."))))

(def plugin {:command "who"
             :author "jayferd"
             :doc ".who [is|add|added] : manage user descriptions"
             :handler (fn [irc message]
                        (->> (extract-word message)
                             (concat [irc message])
                             (apply handle-who)))})
