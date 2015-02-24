(ns goodbot.plugins.health
  "Monitors the health of Production."
  (:require [goodbot.parse :refer [extract-word]]
            [irclj.core :as irclj]
            [clj-http.client :as client]
            [clojure.data :as data]))

(def channel "#goodbot-test")

(def site-health (atom {:up true :maintenance false}))

(defn check-health []
  "Pull mainenance page to determine health of system."
  (let [response
        (client/get "https://www.ulpurview.com/health/maintenance" {:as :json})]
    (if (== 200 (:status response))
        {:up true  :maintenance (get-in response [:body :maintenance])}
        {:up false :maintenance false})))

(defn poll-health [irc]
  "Poll maintenance page of site to determine health of system."
  (let [health (check-health) health-changes (first (data/diff health @site-health))]
    (when (not-empty health-changes)
      (println "CHANGES " health-changes)
      (irclj/message irc channel
        (str "Production "
          (if (contains? health-changes :up)
            (if (get health-changes :up) "is UP." "has fallen DOWN.")
            (if (get health-changes :maintenance)
              "is in MAINTENANCE"
              "MAINTENANCE is complete"))))
      (swap! site-health merge @site-health health))))

(def plugin {:author "davidhampgonsalves"
             :doc {"health" ".health : Checks the current health of the production site."}
             :commands {"health" (fn [irc message]
                (let [health (check-health)]
                  (str "Production is "
                      (if (get health :up) "UP" "DOWN")
                      (when (health :maintenance) " but in MAINTENANCE MODE."))))}
             :tasks [{:name "production health monitor"
                      :interval 5000
                      :work poll-health}]})
