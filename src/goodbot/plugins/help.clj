(ns goodbot.plugins.help
  "Prints documentation for other plugins"
  (:require [goodbot.parse :refer [extract-word]]
            [goodbot.bot :refer [select-plugin]]
            [clojure.string]))

(defn handle-help [irc message]
  (def plugins (:plugins @irc))
  (if-let [[command _] (extract-word message)]
    (if-let [plugin (select-plugin plugins command)]
      (:doc plugin)
      (str "No such plugin ." command))
    (str "Available commands: " (clojure.string/join ", " (map :command plugins)))))

(def plugin {:command "help"
             :doc ".help <command> : print help info for <command>"
             :handler handle-help})
