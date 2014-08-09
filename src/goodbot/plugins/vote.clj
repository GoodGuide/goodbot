(ns goodbot.plugins.vote
  "Sends a PING back given .pong"
  (:require [goodbot.db :as db]
            [datomic.api :as datomic]
            [clojure.algo.generic.functor :refer [fmap]]
            [goodbot.parse :refer [extract-word]]))

(defn votes-about [db name]
  (def results (-> '[:find ?e :in $ ?name :where [?e :vote.entry/name ?name]]
                   (datomic/q db name)))
  (->> results
       (map first)
       (map #(datomic/entity db %))))


(defn votes-of [db name]
  (def results (-> '[:find ?e :in $ ?name :where [?e :vote.entry/voter ?name]]
                   (datomic/q db name)))
  (->> results
       (map first)
       (map #(datomic/entity db %))))

(defn sum-votes [votes]
  (->> votes
       (map :vote.entry/count)
       (reduce +)))

(defn parse-int [str]
  (try (Integer/parseInt str) (catch NumberFormatException _ nil)))

(defn handle-plus-minus [irc vote message]
  (def sign (first vote))
  (def magnitude-string (apply str (rest vote)))
  (def magnitude (if (empty? magnitude-string) 1 (parse-int magnitude-string)))
  (when magnitude
    (def total (if (= \+ (first vote)) magnitude (- 0 magnitude)))
    (def conn (db/get-conn irc))
    @(datomic/transact conn [{:db/id (datomic/tempid :db.part/vote)
                              :vote.entry/name (:text message)
                              :vote.entry/count total
                              :vote.entry/voter (:nick message)}])
    (str "voted " total " for " (:text message))))

(defmulti handle-vote
  (fn [_ c _] c))

(defmethod handle-vote :default [_ _ _] nil)

(defmethod handle-vote "score" [irc _ message]
  (def votes (-> irc
                  db/get-conn
                  datomic/db
                  (votes-about (:text message))))
  (str (:text message) " scores " (sum-votes votes) "."))

(defmethod handle-vote "show" [irc _ message]
  (def votes (-> irc
                 db/get-conn
                 datomic/db
                 (votes-about (:text message))))
  (def summary (->> votes
                    (group-by :vote.entry/voter)
                    (fmap sum-votes)))
  (str "summary for " (:text message) ": " (str summary)))

(defmethod handle-vote "by" [irc _ message]
  (def votes (-> irc
                 db/get-conn
                 datomic/db
                 (votes-of (:text message))))
  (def summary (->> votes
                    (group-by :vote.entry/name)
                    (fmap sum-votes)))
  (str "votes by " (:text message) ": " (str summary)))

(def plugin {:author "jneen"
             :doc {"vote" ".vote [+n|-n] : vote on things"
                   "vote-score" ".vote score <thing> : show the score for <thing>"
                   "vote-show" ".vote show <thing> : show votes about <thing>"
                   "vote-by" ".vote by <person> : show <person>'s votes"}
             :schema "vote.edn"
             :commands {"vote" (fn [irc message]
                                 (def first-char (-> message :text first))
                                 (let [handler (if ((set "+-") first-char)
                                                 handle-plus-minus
                                                 handle-vote)]
                                   (apply handler (concat [irc] (extract-word message)))))}})
