(defproject goodbot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "https://github.com/GoodGuide/goodbot"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [irclj "0.5.0-alpha4"]
                 [com.datomic/datomic-free "0.9.4707"]
                 [org.clojure/algo.generic "0.1.0"]
                 [clojail "1.0.6"]]
  :main goodbot.core)
