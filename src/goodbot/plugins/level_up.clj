(ns goodbot.plugins.level-up
  "Alerts channel when the bot learns a new skill"
  (:require [goodbot.parse :refer [extract-word]]
            [irclj.core :as irclj]
            [goodbot.db :as db]
            [goodbot.bot :as bot]
            [datomic.api :as datomic]))

(defn level-up [irc]
  (println "::::::::::::: getting skills")
  (def prev-skills (db/q irc '[:find ?s
              :where [_ :level-up/skills ?s]]))
  (println prev-skills)
  (println "::::::::::::: retracting skills")
  (comment db/transact irc '[":db/retract" [":level-up/skills"]]) 

  (def skills (concat (bot/get-plugin-commands irc) (:tasks @irc)))
  (println "::::::::::::: adding skills")
  (println "::::: " skills)

  (println ":::::: building query")
  (def add-query (map #((println "map " %){:db/id #db/id[:db.part/level-up] :level-up/skills %}) skills))

  (println ":::::: query " add-query)
  (println "========================")
  (db/transact irc add-query)
  (println "::::: DONE"))
  
(def plugin {:author "davidhampgonsalves"
             :schema "level-up.edn"
             :tasks [{:name "celebrate learning new tricks"
                      :run-at-startup true
                      :work level-up}]})
