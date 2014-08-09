(ns goodbot.plugins.clojure
  "evals snippets of clojure code"
  (:require [irclj.core :as irclj]
            [clojail.core :as clojail]
            [clojail.testers]))

(def sandbox (clojail/sandbox clojail.testers/secure-tester :timeout 5000))

(defn safe-eval-string [code]
  (println "code: " code)
  (try (-> code clojail/safe-read sandbox str)
    (catch Throwable e (str "error: " e))))

(def plugin {:author "jneen"
             :doc {"clj" ".clj <code> : evals the given clojure code"}
             :commands {"clj" (fn [irc message] (-> message :text safe-eval-string))}})
