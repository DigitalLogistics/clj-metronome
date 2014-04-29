(ns clj-metronome.core
  (:require [clojure.core.async :refer [put! <!! >! alts! go-loop timeout close!]]
            [clojure.edn :as edn]
            [clojure.core.match :refer [match]]
            [clojure.core.match.protocols :refer [IMatchLookup]]
            [clj-time.core :as time :refer [today-at plus days]]
            [clj-time.coerce :refer [to-date]]
            [clj-metronome.types :refer :all]
            [clj-metronome.websocket :refer [make-websocket!]]
            [clj-metronome.socketio :refer :all]
            [schema.core :as s]))

(extend-type clojure.lang.ExceptionInfo
  IMatchLookup
  (val-at [this k not-found]
    (case k
      :error? true
      not-found)))

(defn await-data-message
  [{:keys [send-channel
           receive-channel]
    :as socket} name max-time]
  (let [ttl (timeout max-time)]
    (go-loop []
      (let [[message channel] (alts! [receive-channel ttl])]
        (match channel
               receive-channel (let [parsed (parse-socketio-message message)]
                                 (match parsed
                                        {:error? true} parsed
                                        {:type :heartbeat} (recur)
                                        {:type :event} (do
                                                         (>! send-channel "2::")
                                                         (recur))
                                        {:type :data
                                         :payload {:name name :args [arg]}} arg
                                         :else (ex-info "No matching clause."
                                                        {:message parsed})))
               ttl (ex-info "Got timeout awaiting message."
                            {:timeout max-time
                             :name name}))))))

(defn await-valid-response
  [socket name schema ttl]
  (let [message (<!! (await-data-message socket name ttl))]
    (match message
           {:error? true} (throw message)
           :else (do
                   (s/validate schema message)
                   (printf "Validated: %s\n" name)))))

(defn -main
  ([]
     (-main "api.fynder.io:8001" "5000"))
  ([server-name ttl]
     (let [socket (make-websocket! server-name)
           ttl (edn/read-string ttl)]
       (try
         (send-message! socket
                        "schedules.requested"
                        ScheduleRequest
                        {:businessId 1
                         :location {:id 1}
                         :date {:start (to-date (plus (today-at 0 00) (days -2)))
                                :end   (to-date (plus (today-at 0 00) (days  2)))}})

         (await-valid-response socket
                               "schedules.changed"
                               [FynderClass]
                               ttl)

         (finally
           (println "Closing websocket.")
           (close! (:send-channel socket)))))))
