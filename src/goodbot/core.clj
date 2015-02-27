(ns goodbot.core
  "Chatbot for GoodGuide"
  (:require [goodbot.bot :as bot]
            [goodbot.db :as db]
            [clojure.tools.namespace.find :as namespace-tools]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.walk :only keywordize-keys :as walk]))

(defn get-channels []
  (let [ch (str/split (or (System/getenv "GOODBOT_CHANNELS") "") #" ")
        ch (->> (if (= (count ch) 1) [] ch)
                (#(apply hash-map %1))
                walk/keywordize-keys)]
    (if-not (contains? ch :fallback) (conj ch [:fallback "#goodbot-test"]) ch)))

(defn run [plugins]
  "runs a bot with the given plugins with configuration based on environment variables"
  (let [[host port] (-> (System/getenv "GOODBOT_HOST")
                        (or "irc.freenode.net:6667")
                        (.split ":" 2))
        channels (-> (get (System/getenv) "GOODBOT_CHANNELS" "#goodbot-test")
                     (.split ",")
                     vec)
        nick (-> (System/getenv "GOODBOT_NICK") (or "goodbot-test"))
        server-password (System/getenv "GOODBOT_SERVER_PASSWORD")
        password (System/getenv "GOODBOT_PASSWORD")
        ssl? (or (System/getenv "GOODBOT_SSL_ENABLED") false)
        channels (get-channels)
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
                       :ssl? false
                       :nick nick
                       :password password
                       :channels channels
                       :server-password server-password
                       :datomic-uri datomic-uri)))

(defn load-plugin-symbols []
  (namespace-tools/find-namespaces-in-dir (io/file "src/goodbot/plugins")))

(defn load-plugins []
  (let [symbols (load-plugin-symbols)]
    (doseq [ns symbols] (require ns)) ; require all the plugin namespaces
    (map (fn [plugin] (deref (ns-resolve plugin 'plugin))) symbols)))

(defn run-task [task-name]
  (def plugins (load-plugins))
  (def irc (ref {:datomic-uri "datomic:mem://goodbot"
                 :plugins plugins}))
  (println " - setting up database")
  (db/start irc)
  (println " - database setup complete")
  (def task (first (filter #(= (:name %) task-name) (flatten (filter identity (map #(:tasks %) plugins))))))
  (println " - running " task-name)
  (if task ((:work task) irc) (println " - " task-name " not found"))
  (println " - task complete"))

(defn -main
  "Starts the bot"
  [& args]
    (run (load-plugins)))
