(ns goodbot.core
  "Chatbot for GoodGuide"
  (:require [irclj.core :as irclj]
            [goodbot.bot :as bot]
            [goodbot.plugins.ping :as ping]
            [goodbot.plugins.clojure :as clj]))

(defn -main
  "Starts the bot"
  [& args]
  (let [bot (irclj/connect "irc.freenode.net" 6667 "goodbot-test"
                           :callbacks {:privmsg (bot/make-callback
                                                  [ping/plugin
                                                   clj/plugin])})]
    (irclj/join bot "#csuatest")))
