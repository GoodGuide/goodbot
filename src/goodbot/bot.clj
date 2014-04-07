(ns goodbot.bot
  "Wrapper utility to make a bot"
  [:require
   [irclj.core :as irclj]
   [goodbot.parse :refer [extract-command extract-word]]])

(defn select-plugin [plugins name]
  (->> plugins (filter #(= name (:command %)) first)))

(defn add-help [plugins]
  (declare plugins-with-help)
  (defn help-handler [irc message]
    (defn handle [command]
      (if-let [plugin (select-plugin plugins-with-help command)]
        (or (:doc plugin)
            (str "." command " doesn't have documentation!"))
        (str "unknown command ." command)))
    (if-let [[command _] (extract-word message)]
      (handle command)
      (handle "help")))
  (def help-plugin
    {:command "help"
     :doc ".help <command> : print documentation for the given command"
     :handler help-handler})
  (def plugins-with-help (conj plugins help-plugin))
  plugins-with-help)

(defn respond-with [irc message responses]
  (when-not (nil? responses)
    (def vec-responses (if (coll? responses) responses [responses]))
    (doseq [r vec-responses]
      (println "RESPONDING:" r)
      (irclj/reply irc message r))))

(defn make-callback [plugins]
  (def plugins-with-help (add-help plugins))
  (fn [irc message]
    (when-let [[command rest-of-text] (extract-command message)]
      (println "COMMAND: " command rest-of-text)
      (when-let [plugin (select-plugin plugins-with-help command)]
        (def updated-message (assoc message :text rest-of-text) )
        (def handler (:handler plugin))
        (when-let [responses (handler irc updated-message)]
          (respond-with irc message responses))))))
