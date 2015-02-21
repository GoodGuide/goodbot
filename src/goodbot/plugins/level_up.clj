(ns goodbot.plugins.level-up
  "Alerts channel when the bot learns a new skill"
  (:require [goodbot.parse :refer [extract-word]]
            [irclj.core :as irclj]
            [goodbot.db :as db]
            [datomic.api :as datomic]))

(defn level-up [irc]
  (println "::::RUNNING::::::"))

(def plugin {:author "davidhampgonsalves"
             :schema "level-up.edn"
             :tasks [{:name "celebrate learning new tricks"
                      :interval 0
                      :work level-up}]})
