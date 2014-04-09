(ns goodbot.plugins.karma
  "Sends a PING back given .pong"
  (:require [irclj.core :as irclj]
            [goodbot.parse :refer [extract-word]]))

(defmulti handle-karma
  (fn [_ c _] c))

(defmethod handle-karma "+" [irc _ text]
  (str "TODO: " text))

(def plugin {:command "karma"
             :author "jayferd"
             :doc ".karma : TODO"
             :handler (fn [irc message]
                        (apply handle-karma (concat [irc] (extract-word message))))})
