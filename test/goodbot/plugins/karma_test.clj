(ns goodbot.plugins.karma-test
  (:require [clojure.test :refer :all]
            [goodbot.plugins.karma :as karma]))

(deftest handle-karma
  (testing "+"
    (is (= (karma/handle-karma (ref {}) "+" "thing")
           "TODO: thing"))))
