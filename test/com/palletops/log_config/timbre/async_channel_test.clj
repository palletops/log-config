(ns com.palletops.log-config.timbre.async-channel-test
  (:require
   [clojure.core.async :refer [<!! chan close! sliding-buffer]]
   [clojure.test :refer :all]
   [com.palletops.log-config.timbre
    :refer [domain-msg logging-threshold-fixture with-domain]]
   [com.palletops.log-config.timbre.async-channel :refer :all]
   [taoensso.timbre :as timbre]))

(use-fixtures :once (logging-threshold-fixture))

(deftest memory-sink-test
  (testing "default keys"
    (let [m (atom nil)
          c (chan (sliding-buffer 2))
          sink (memory-sink c m 3)]
      (timbre/merge-config!
       {:appenders {:async (make-async-channel-appender c {})}
        :middlware nil})
      (timbre/logf :debug "Hello %s" 'there {:x 1})
      (close! c)
      (is (= [{:throwable nil
               :args ["Hello %s" 'there {:x 1}]
               :ns "com.palletops.log-config.timbre.async-channel-test"}]
             (map #(dissoc % :hostname) (<!! sink))))))
  (testing "with domain"
    (let [m (atom nil)
          c (chan (sliding-buffer 2))
          sink (memory-sink c m 3)]
      (timbre/merge-config!
       {:appenders {:async (make-async-channel-appender
                            c {:kws [:hostname :ns :args :throwable
                                     :profile-stats :domain]})}
        :middleware [domain-msg]})
      (with-domain :d
        (timbre/logf :debug "Hello %s" 'there {:x 1}))
      (close! c)
      (is (= [{:throwable nil
               :args ["Hello %s" 'there {:x 1}]
               :ns "com.palletops.log-config.timbre.async-channel-test"
               :domain :d}]
             (map #(dissoc % :hostname) (<!! sink)))))))
