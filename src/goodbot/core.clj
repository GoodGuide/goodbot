(ns goodbot.core
  "Chatbot for GoodGuide"
  (:require [goodbot.bot :as bot]
            [goodbot.plugins.ping]
            [goodbot.plugins.clojure]
            [goodbot.plugins.karma]
            [goodbot.plugins.data]
            [goodbot.plugins.help]))

(defn -main
  "Starts the bot"
  [& args]
  (let [[host port] (-> (System/getenv "GOODBOT_HOST")
                        (or "irc.freenode.net:6667")
                        (.split ":" 2))
        channels (-> (System/getenv "GOODBOT_CHANNELS")
                     (or "#goodbot-test")
                     (.split ",")
                     vec)
        nick (-> (System/getenv "GOODBOT_NICK") (or "goodbot-test"))
        server-password (System/getenv "GOODBOT_SERVER_PASSWORD")
        password (System/getenv "GOODBOT_PASSWORD")
        datomic-uri (-> (System/getenv "GOODBOT_DATOMIC")
                        ; detect docker link
                        (or (when-let [link-uri (System/getenv "TRANSACTOR_PORT")]
                              (-> link-uri
                                  (.replace "tcp" "datomic:free")
                                  (str "/goodbot"))))
                        ; default to in-memory
                        (or "datomic:mem://goodbot"))]
    (goodbot.bot/start [goodbot.plugins.ping/plugin
                        goodbot.plugins.clojure/plugin
                        goodbot.plugins.karma/plugin
                        goodbot.plugins.data/plugin
                        goodbot.plugins.help/plugin]
                       :host host
                       :port (Integer/parseInt port)
                       :nick nick
                       :password password
                       :channels channels
                       :server-password server-password
                       :datomic-uri datomic-uri)))
