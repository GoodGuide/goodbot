(ns goodbot.plugins.clojure
  "evals snippets of clojure code"
  (:require [irclj.core :as irclj]
            [clojail.core :as clojail]
            [clojail.testers]))

(def sandbox (clojail/sandbox clojail.testers/secure-tester :timeout 5000))

(defn safe-eval-string [code]
  (println "code: " code)
  (-> code clojail/safe-read sandbox))

(def plugin {:command "clj"
             :doc ".clj <code> : evals the given clojure code"
             :handler (fn [irc message] (-> message :text safe-eval-string))})
