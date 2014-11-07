(ns goodbot.bot
  "Wrapper utility to make a bot"
  [:require
   [irclj.core :as irclj]
   [irclj.events]
   [goodbot.db :as db]
   [goodbot.parse :refer [extract-command]]])

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

(defn start [plugins & {:keys [host port nick password
                               channels server-password
                               datomic-uri]}]
  (println (str "connecting to " host ":" port " as " nick " with password " server-password))
  (def bot (irclj/connect host port nick
                          :pass server-password
                          :callbacks {:privmsg (privmsg-callback plugins)
                                      :raw-log irclj.events/stdout-callback}))
  (dosync
    (alter bot assoc
           :prefixes {}
           :datomic-uri datomic-uri
           :ssl? true
           :plugins plugins)
    (println "connecting to datomic at" datomic-uri)
    (db/start bot))
  (when password (irclj/identify bot password))
  (doseq [c channels] (println "joining" c) (irclj/join bot c)))
