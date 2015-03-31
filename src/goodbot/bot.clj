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
    (println (:target message))
    (println message)
    (let [message (if (.startsWith (:target message) "#") message (assoc message :target (:user message)))
          vec-responses (if (coll? responses) responses [responses])]
      (println (:target message))
      (doseq [r vec-responses]
        (irclj/reply irc message r)))))

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

(defn schedule-tasks [bot plugins]
  "Schedule all plugin tasks"
  (def warm-up-delay 10000)
  (def task-scheduler-pool (at/mk-pool))
  (doseq [plugin plugins]
    (doseq [task (get plugin :tasks)]
      (def work #((:work task) bot))
      (if (contains? task :interval) 
        (at/every (:interval task) work task-scheduler-pool :initial-delay warm-up-delay))
      (if (contains? task :run-at-startup)
        (at/after warm-up-delay work task-scheduler-pool)))))

(defn get-plugin-commands [bot]
  (mapcat #(keys (:commands %)) (:plugins @bot)))

(defn get-task-names [plugins] (map :name (mapcat :tasks plugins)))

(defn message-channel [bot requested-channel messages]
  (def messages (if-not (seq? messages) [messages] messages))
  (def channels (:channel-names @bot))
  (let [channel (if (contains? channels requested-channel) requested-channel :fallback)]
    (if (= channel :fallback) (println "No channel is set for " requested-channel " using fallback.")) 
    (doseq [message messages]
      (irclj/message bot (get channels channel) message))))

(defn message-nick [bot nick message]
  (irclj/message bot nick message))

(defn start [plugins & {:keys [host port nick password ssl?
                               channels server-password
                               datomic-uri]}]
  (println "connecting to " host ":" port " as " nick " with password " server-password (if ssl? " using ssl") ".")
  (def bot (irclj/connect host port nick
                          :pass server-password
                          :callbacks {:privmsg (privmsg-callback plugins)
                                      :raw-log irclj.events/stdout-callback}
                          :ssl? ssl?))
  (def tasks (get-task-names plugins))
  (dosync
    (alter bot assoc
           :prefixes {}
           :datomic-uri datomic-uri
           :plugins plugins
           :tasks  tasks
           :channel-names channels))
  (println "Commands :" (str/join ", " (get-plugin-commands bot)))
  (println "Tasks    :" (str/join ", " tasks))
  (println "Datomic  :" datomic-uri)
  (println "Channels :" (str/join ", " (vals channels)))
  (db/start bot)
  (when password (irclj/identify bot password))
  (doseq [c (vals channels)] (println "Joining" c) (irclj/join bot c))
  (schedule-tasks bot plugins))
