(ns goodbot.bot
  "Wrapper utility to make a bot"
  [:require
   [irclj.core :as irclj]
   [goodbot.parse :refer [extract-command extract-word]]])

(defn select-plugin [plugins name]
  (->> plugins (filter #(= (:command %) name)) first))

(defn make-help [plugins]
  (declare plugins-with-help)
  (defn help-handler [irc message]
    (defn handle [command]
      (println "plugins" plugins-with-help)
      (if-let [plugin (select-plugin plugins-with-help command)]
        (irclj/reply irc message
                     (or (:doc plugin)
                         (str "." command " doesn't have documentation!")))
        (irclj/reply irc message
                     (str "unknown command ." command))))
    (if-let [[command _] (extract-word message)]
      (handle command)
      (handle "help")))
  (def help-plugin
    {:command "help"
     :doc ".help <command> : print documentation for the given command"
     :handler help-handler})
  (def plugins-with-help (conj plugins help-plugin))
  help-plugin)

(defn make-callback [plugins]
  (def plugins-with-help (conj plugins (make-help plugins)))
  (fn [irc message]
    (when-let [[command rest-of-text] (extract-command message)]
      (println "COMMAND: " command)
      (when-let [plugin (select-plugin plugins-with-help command)]
        (let [updated-message (assoc message :text rest-of-text)
              handler (:handler plugin)]
          (handler irc updated-message))))))
