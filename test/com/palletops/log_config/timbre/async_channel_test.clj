(ns com.palletops.log-config.timbre.async-channel-test
  (:require
   [clojure.core.async :refer [<!! chan close! sliding-buffer]]
   [clojure.test :refer :all]
   [com.palletops.log-config.timbre :refer [logging-threshold-fixture]]
   [com.palletops.log-config.timbre.async-channel :refer :all]
   [taoensso.timbre :as timbre]))

(use-fixtures :once (logging-threshold-fixture))

(deftest memory-sink-test
  (let [m (atom nil)
        c (chan (sliding-buffer 2))
        sink (memory-sink c m 3)]
    (timbre/merge-config!
     {:appenders {:async (make-async-channel-appender c {})}})
    (timbre/logf :debug "Hello %s" 'there {:x 1})
    (close! c)
    (is (= [{:throwable nil
             :args ["Hello %s" 'there {:x 1}]
             :ns "com.palletops.log-config.timbre.async-channel-test"}]
           (map #(dissoc % :hostname) (<!! sink))))))
