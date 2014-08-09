(ns goodbot.plugins.help
  "Prints documentation for other plugins"
  (:require [goodbot.parse :refer [extract-word]]
            [clojure.string :refer [join]]))

(defn select-doc [plugins word]
  (print "select-doc" plugins word)
  (->> plugins (map #(get-in % [:doc word])) (remove nil?) first))

(defn handle-help [irc message]
  (def plugins (:plugins @irc))
  (if-let [[command _] (extract-word message)]
    (or (select-doc plugins command)
        (str "No documentation for \"" command "\""))
    (str "Available topics " (join ", " (->> plugins (mapcat (comp keys :doc)) sort)))))

(def plugin {:author "jneen"
             :doc {"help" ".help <command> : print help info for <command>"}
             :commands {"help" handle-help}})
