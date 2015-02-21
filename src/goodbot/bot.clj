(ns goodbot.bot
  "Wrapper utility to make a bot"
  [:require
   [irclj.core :as irclj]
   [irclj.events]
   [goodbot.db :as db]
   [goodbot.parse :refer [extract-command]]
   [overtone.at-at :as at]
   [clojure.string :as str]])


(defn select-handler [plugins name]
  (->> plugins (map #(get-in % [:commands name])) (remove nil?) first))

(defn respond-with [irc message responses]
  (when-not (nil? responses)
    (def vec-responses (if (coll? responses) responses [responses]))
    (doseq [r vec-responses]
      (irclj/reply irc message r))))

(defn privmsg-callback [plugins]
  (fn [irc message]
    (try
      (when-let [[command updated-message] (extract-command message)]
        (println "COMMAND: " command (str [(:text message)]))
        (if-let [handler (select-handler plugins command)]
          (when-let [responses (handler irc updated-message)]
            (respond-with irc updated-message responses))
          (respond-with irc updated-message
            (str "Sorry, I'm not smart enough to "
              (get updated-message :command) ". Try .help instead."))))
      (catch Throwable e
        (irclj/reply irc message (str "error: " e))
        (println (.getMessage e))
        (.printStackTrace e)))))

(defn schedule-tasks [bot, plugins]
  "Schedule all plugin tasks"
  (def warm-up-delay 5000)
  (def task-scheduler-pool (at/mk-pool))
  (doseq [plugin plugins]
    (doseq [task (get plugin :tasks)]
      (if (contains? task :interval) 
        (at/every (:interval task)
            #((:work task) bot)
            task-scheduler-pool
            :fixed-delay true
            :initial-delay warm-up-delay]))
      (if (contains? task :run-at-startup)
        (at/after 
          warm-up-delay
          #((:work task) bot)
          task-scheduler-pool))))

(defn get-plugin-commands [plugins]
  (mapcat #(keys (:commands %)) plugins))

(defn message-channel [bot channel message]
  (def channels (:channels bot))
  (let [ch (if (contains? channels channel) channel :fallback)]
    (if (= channel :fallback) (println "No channel is set for " channel " using fallback.")) 
    (irclj/message bot (get channels ch) message)))

(defn message-nick [bot nick message]
  (irclj/message bot (str "@" nick) message))

(defn start [plugins & {:keys [host port nick password ssl?
                               channels server-password
                               datomic-uri]}]
  (println "connecting to " host ":" port " as " nick " with password " server-password (if ssl? " using ssl") ".")
  (def bot (irclj/connect host port nick
                          :pass server-password
                          :callbacks {:privmsg (privmsg-callback plugins)
                                      :raw-log irclj.events/stdout-callback}
                          :ssl? ssl?))
  (dosync
    (alter bot assoc
           :prefixes {}
           :datomic-uri datomic-uri
           :ssl? ssl?
           :plugins plugins
           :channels channels)
  (println "Commands : " (str/join ", " (get-plugin-commands plugins)))
  (println "Tasks    : " (str/join ", " (map :name (mapcat :tasks plugins))))
  (println "Datomic  : " datomic-uri)
  (println "Channels : " (str/join ", " (vals channels)))
  (db/start bot))
  (when password (irclj/identify bot password))
  (doseq [c (vals channels)] (println "joining" c) (irclj/join bot c))
  (schedule-tasks bot plugins))
