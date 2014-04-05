(ns goodbot.plugins.ping
  "Sends a PING back given .pong"
  (:require [irclj.core :as irclj]
            [goodbot.parse :only [extract-command]]))

(def plugin {:command "ping"
             :author "jayferd"
             :doc ".ping : responds with \"pong\""
             :handler (fn [irc message] "pong")})
