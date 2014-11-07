(ns goodbot.core
  "Chatbot for GoodGuide"
  (:require [goodbot.bot :as bot]
            [clojure.tools.namespace.find :as namespace-tools]
            [clojure.java.io :as io]))

(defn run [plugins]
  "runs a bot with the given plugins with configuration based on environment variables"
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
    (goodbot.bot/start plugins
                       :host host
                       :port (Integer/parseInt port)
                       :nick nick
                       :password password
                       :channels channels
                       :server-password server-password
                       :datomic-uri datomic-uri)))

(defn get-plugin-symbols []
  (namespace-tools/find-namespaces-in-dir (io/file "src/goodbot/plugins")))

(defn -main
  "Starts the bot"
  [& args]
  (let [plugins (get-plugin-symbols)]
    (println "Loaded plugins: ")
    (doseq [plugin plugins] (println "  " plugin))
    (doseq [ns plugins] (require ns)) ; require all the plugin namespaces
    (run (map (fn [plugin] (deref (ns-resolve plugin 'plugin))) plugins))))

