(ns clj-metronome.socketio-test
  (:require [clojure.test :refer :all]
            [clj-metronome.socketio :refer :all]))

(deftest parse-socketio-message-test
  (are [message value] (= (parse-socketio-message message)
                          value)
       "1::"
       {:type :heartbeat}

       "2::"
       {:type :event}

       "5:::{\"name\":\"bookings.requested\",\"args\":[{\"businessId\":1}]}"
       {:type :data
        :payload {:name "bookings.requested"
                  :args [{:businessId 1}]}}))
