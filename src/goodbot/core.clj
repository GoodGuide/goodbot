(ns goodbot.core
  "Chatbot for GoodGuide"
  (:require [irclj.core :as irclj]
            [irclj.events]
            [goodbot.bot :as bot]
            [goodbot.plugins.ping :as ping]
            [goodbot.plugins.clojure :as clj]))

(def host     (or (System/getenv "GOODBOT_HOST")    "irc.freenode.net"))
(def port     (or (System/getenv "GOODBOT_PORT")    6667))
(def nick     (or (System/getenv "GOODBOT_NICK")    "goodbot-test"))
(def channel  (or (System/getenv "GOODBOT_CHANNEL") "#goodbot-test"))
(def server-password (or (System/getenv "GOODBOT_SERVER_PASSWORD") nil))
(def password (or (System/getenv "GOODBOT_PASSWORD") nil))

(defn -main
  "Starts the bot"
  [& args]
  (println (str "connecting to " host ":" port channel " as " nick " with password " server-password))
  (let [bot (irclj/connect host port nick
                           :pass server-password
                           :callbacks {:privmsg (bot/make-callback
                                                  [ping/plugin
                                                   clj/plugin])
                                       :raw-log irclj.events/stdout-callback})]
    ; XXX HACK remove me once prefixes gets defaulted in irclj (and released) ! XXX
    (dosync (alter bot assoc :prefixes {}))

    (irclj/identify bot password)
    (irclj/join bot channel)))
