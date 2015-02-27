(ns goodbot.plugins.level-up
  "Alerts channel when the bot learns a new skill"
  (:require [goodbot.parse :refer [extract-word]]
            [irclj.core :as irclj]
            [goodbot.db :as db]
            [goodbot.bot :as bot]
            [clojure.string :as str]
            [datomic.api :as datomic]))

(defn level-up-message [skills new-skills]
  (into [(str "goodbot grew to level *" (count skills) "!* :clap:")]
    (->> new-skills
      (map #(str "> learned *" % "*!")))))

(defn level-up [irc]
  (let [prev-skills (db/q irc '[:find ?s
              :where [_ :level-up/skills ?s]])
        prev-skills (flatten (into set prev-skills))
        skills (concat (bot/get-plugin-commands irc) (:tasks @irc))
        new-skills (clojure.set/difference (set skills) prev-skills)] 
  
    (if-not (empty? new-skills) 
        (bot/message-channel irc :general (level-up-message skills new-skills)))

    (if-not (empty? prev-skills)
      (db/transact irc [[":db.fn/retractEntity" [:level-up/skills "_"]]]))
  
    (def add-query (mapv (fn [skill] {:db/id #db/id[:db.part/level-up] :level-up/skills skill}) skills))

    (db/transact irc add-query)))

  
(def plugin {:author "davidhampgonsalves"
             :schema "level-up.edn"
             :tasks [{:name "level-up"
                      :run-at-startup true
                      :work level-up}]})
