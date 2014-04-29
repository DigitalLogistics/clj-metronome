(ns clj-metronome.websocket
  (:require [clojure.core.async :refer [chan put! <! >! map< alts! go go-loop timeout close!]]
            [gniazdo.core :as ws]))

(defn make-websocket!
  "Creates a websocket to the given server:port, and returns a map of two channels.
  :receive-channel contains messages from the server.
  :send-channel is for messages you want to send to the server. When you close this channel, the connection will be closed."
  [server-name]
  (let [send-channel (chan 5)
        receive-channel (chan 5)
        url (format "ws://%s/socket.io/1/websocket/%s"
                    server-name
                    (gensym))
        socket (ws/connect url
                           :on-connect (fn [session]
                                         (printf "Connected to: %s\n" url)
                                         (.setMaxTextMessageSize (.getPolicy session)
                                                                 200000))
                           :on-receive #(put! receive-channel %)
                           :on-error #(put! receive-channel (ex-info "Websocket Error"
                                                                     {:error %})))]

    (go-loop []
      (if-let [message (<! send-channel)]
        (do
          (printf "Sending Websocket Message: %s\n" message)
          (ws/send-msg socket message)
          (recur))
        (do
          (close! receive-channel)
          (ws/close socket))))

    {:send-channel send-channel
     :receive-channel receive-channel}))
