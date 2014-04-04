(ns goodbot.parse-test
  (:require [clojure.test :refer :all]
            [goodbot.parse :as parse]))

(deftest extract-command
  (testing "when there is a command"
    (is (= (parse/extract-command {:text ".foo bar baz"})
           ["foo" "bar baz"])))

  (testing "when there is a non-word command"
    (is (= (parse/extract-command {:text ".++ jneen"})
           ["++" "jneen"]))

  (testing "when there is not a command"
    (is (nil? (parse/extract-command {:text "foo bar baz"}))))))

(deftest extract-word
  (testing "when there are words"
    (is (= (parse/extract-word {:text "foo bar baz"})
           ["foo" "bar baz"])))

  (testing "when there is only whitespace"
    (is (nil? (parse/extract-word {:text "     "})))))

