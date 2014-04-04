(ns goodbot.parse
  "Parsing utilities")

(defn extract-command [message]
  (if-let [[_ cmd rest-of-text]
           (re-find #"^[.](\S+)\s*(.*)$" (:text message))]
    [cmd rest-of-text]))

(defn extract-word [message]
  (if-let [[_ word rest-of-text]
           (re-find #"^(\S+)\s*(.*)$" (:text message))]
    [word rest-of-text]))

