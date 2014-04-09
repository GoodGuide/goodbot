(ns goodbot.core
  "Chatbot for GoodGuide"
  (:require [goodbot.bot :as bot]
            [goodbot.plugins.ping]
            [goodbot.plugins.clojure]
            [goodbot.plugins.karma]
            [goodbot.plugins.data]
            [goodbot.plugins.help]))

(def host     (or (System/getenv "GOODBOT_HOST")    "irc.freenode.net"))
(def port     (or (System/getenv "GOODBOT_PORT")    6667))
(def nick     (or (System/getenv "GOODBOT_NICK")    "goodbot-test"))
(def channel  (or (System/getenv "GOODBOT_CHANNEL") "#goodbot-test"))
(def server-password (or (System/getenv "GOODBOT_SERVER_PASSWORD") nil))
(def password (or (System/getenv "GOODBOT_PASSWORD") nil))
(def datomic-uri (or (System/getenv "GOODBOT_DATOMIC") "datomic:mem://goodbot"))

(defn -main
  "Starts the bot"
  [& args]
  (goodbot.bot/start [goodbot.plugins.ping/plugin
                      goodbot.plugins.clojure/plugin
                      goodbot.plugins.karma/plugin
                      goodbot.plugins.data/plugin
                      goodbot.plugins.help/plugin]
                       :host host
                       :port port
                       :nick nick
                       :password password
                       :channels [channel]
                       :server-password server-password
                       :datomic-uri datomic-uri))
