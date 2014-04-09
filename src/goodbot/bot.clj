(ns goodbot.bot
  "Wrapper utility to make a bot"
  [:require
   [irclj.core :as irclj]
   [irclj.events]
   [goodbot.db :as db]
   [goodbot.parse :refer [extract-command extract-word]]])

(defn select-plugin [plugins name]
  (->> plugins (filter #(= name (:command %))) first))

(defn respond-with [irc message responses]
  (when-not (nil? responses)
    (def vec-responses (if (coll? responses) responses [responses]))
    (doseq [r vec-responses]
      (irclj/reply irc message r))))

(defn privmsg-callback [plugins]
  (fn [irc message]
    (try
      (when-let [[command rest-of-text] (extract-command message)]
        (println "COMMAND: " command rest-of-text)
        (when-let [plugin (select-plugin plugins command)]
          (def updated-message (assoc message :text rest-of-text) )
          (def handler (:handler plugin))
          (when-let [responses (handler irc updated-message)]
            (respond-with irc message responses))))
      (catch Throwable e
        (irclj/reply irc message (str "error: " e))))))

(defn start [plugins & {:keys [host port nick password
                               channels server-password
                               datomic-uri]}]
  (println (str "connecting to " host ":" port " as " nick " with password " server-password))
  (def bot (irclj/connect host port nick
                          :pass server-password
                          :callbacks {:privmsg (privmsg-callback plugins)
                                      :raw-log irclj.events/stdout-callback}))
  (dosync (alter bot assoc
                 :prefixes {}
                 :datomic-uri datomic-uri
                 :plugins plugins))
  (db/start bot)
  (when password (irclj/identify bot password))
  (doseq [c channels] (println "joining" c) (irclj/join bot c)))