(ns goodbot.core
  "Chatbot for GoodGuide"
  (:require [irclj.core :as irclj]
            [goodbot.bot :as bot]
            [goodbot.plugins.ping :as ping]
            [goodbot.plugins.clojure :as clj]))

(def host    (or (System/getenv "GOODBOT_HOST")    "irc.freenode.net"))
(def port    (or (System/getenv "GOODBOT_PORT")    6667))
(def nick    (or (System/getenv "GOODBOT_NICK")    "goodbot-test"))
(def channel (or (System/getenv "GOODBOT_CHANNEL") "#goodbot-test"))

(defn -main
  "Starts the bot"
  [& args]
  (println (str "connecting to " host ":" port channel " as " nick))
  (let [bot (irclj/connect host port nick
                           :callbacks {:privmsg (bot/make-callback
                                                  [ping/plugin
                                                   clj/plugin])})]
    (irclj/join bot channel)))
