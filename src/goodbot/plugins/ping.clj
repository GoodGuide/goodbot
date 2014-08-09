(ns goodbot.plugins.ping
  "Sends a PING back given .pong")

(def plugin {:command "ping"
             :author "jayferd"
             :doc {"ping" ".ping : responds with \"pong\""}
             :commands {"ping" (fn [irc message] "pong")}})
