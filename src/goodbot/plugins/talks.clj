(ns goodbot.plugins.talks
  "Manages our tech talk schedule"
  (:require [goodbot.parse :refer [extract-word]]
            [goodbot.bot :as bot]
            [goodbot.db :as db]
            [datomic.api :as datomic]))

(defn is-thursday? []
  (= (.getDayOfWeek (java.time.LocalDateTime/now)) (java.time.DayOfWeek/THURSDAY)))

(defn is-hour [hour]
  (= (.getHour (java.time.LocalDateTime/now) hour)))

(defn fetch-presenter-list [irc]
  (def presenters (db/q irc '[:find ?id ?nick ?last-presented 
                     :where [?id :talks.presenter/nick ?nick] 
                            [?id :talks.presenter/last-presented ?last-presented]]))
  (sort-by :last-presented (map #(zipmap [:id :nick :last-presented] %1) presenters)))

(defn fetch-presenter [irc]
  (first (fetch-presenter-list irc)))

(defn add-presenter [irc msg]
  (try
    (do
      (db/transact irc [{:db/id (datomic/tempid :db.part/talks)
                      :talks.presenter/nick (:nick msg)
                      :talks.presenter/last-presented (java.util.Date.)}])
      "you are now a presenter.")
    (catch Exception e (str "you were already a presenter (" (.getMessage e) ")."))))

(defn remove-presenter [irc msg]
 (try
   (do
    (db/transact irc [[":db.fn/retractEntity" [":talks.presenter/nick" (:nick msg)]]])
    "you are no longer a presenter.")
   (catch Exception e "you were not a presenter (" (.getMessage e) ")."))) 

(defn print-presenters[irc msg] 
  (comment "new lines break IRC but slack likes them") 
  (reduce
    #(conj %1 (str (if (empty? %1) ">" "-") " " (:nick %2) "\n"))
    []
    (fetch-presenter-list irc)))

(defn notify-presenter [irc]
  (def presenter (fetch-presenter irc))
  (if (and (not (nil? presenter)) (is-thursday?) (is-hour 10))
   (bot/message-nick irc (:nick presenter) "You are the tech talk presenter for today, if you don't have a talk/video in mind check the wiki. https://goodguide.atlassian.net/wiki/display/E/Tech+Talks")))

(defn mark-presentation-completed [irc]
  (def now (java.time.LocalDateTime/now))
  (if (and (is-thursday?) (is-hour 12))
    (db/transact irc [{:db/id (:id (fetch-presenter irc)) 
                    :talks.presenter/last-presented (java.util.Date.)}])))

(def plugin {:author "davidhampgonsalves"
             :doc {"talks" ".talks : manages our tech talk schedule."
                   "talks-add" ".talks-add : add you to the presenter list."
                   "talks-remove" ".talks-remove : remove you from the presenter list."}
             :schema "talks.edn"
             :commands {"talks" print-presenters 
                        "talks-add" add-presenter
                        "talks-remove" remove-presenter}
             :tasks [{:name "track presenters"
                      :interval (* 60 60 1000)
                      :work mark-presentation-completed}
                     {:name "notify presenter"
                      :interval (* 60 60 1000)
                      :work notify-presenter}]})
