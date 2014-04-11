(ns goodbot.plugins.ping
  "Sends a PING back given .pong")

(def plugin {:command "ping"
             :author "jayferd"
             :doc ".ping : responds with \"pong\""
             :handler (fn [irc message] "pong")})
