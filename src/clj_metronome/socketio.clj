(ns clj-metronome.socketio
  (:require [clojure.core.async :refer [put! <!! >! alts! go-loop timeout close!]]
            [cheshire.core :refer [generate-string parse-string]]
            [schema.core :as s]))

(defn parse-socketio-message
  [message]
  (if-let [[[whole id _ remainder]] (re-seq #"(\d+)::(:(.*))?" message)]
    (case id
      "1" {:type :heartbeat}
      "2" {:type :event}
      "5" {:type :data
           :payload (parse-string remainder true)})
    (ex-info "Unrecognised message"
             {:message message})))

(defn- encode-socketio-data
  [name args]
  (format "5:::%s"
          (generate-string {:name name
                            :args [args]})))

(defn send-message!
  [socket name schema payload]
  (s/validate schema payload)
  (put! (:send-channel socket)
        (encode-socketio-data name payload)))
