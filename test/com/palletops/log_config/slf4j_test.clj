(ns com.palletops.log-config.slf4j-test
  (:require
   [clojure.test :refer :all]
   [taoensso.timbre :refer [example-config info log]]
   [com.palletops.log-config.tools-logging :refer :all]
   [com.palletops.log-config.timbre
    :refer [add-var format-with-context]]
   [com.palletops.log-config.timbre.tools-logging
    :refer [make-tools-logging-appender with-context]]
   [com.palletops.log-config.tools-logging
    :refer [logging-to-string]]))

(def v {:m 1})

(deftest ^:slf4j with-context-test
  (testing "with context aware formatter"
    (let [config (merge example-config
                        {:fmt-output-fn format-with-context
                         :appenders {:slf4j (make-tools-logging-appender {})}})]
      (testing "without context setting middleware"
        (is (not
             (re-find
              #":m 1\n$"
              (logging-to-string
               (with-context {:m 1}
                 (log config :info "something")))))
            "log message doesn't contain context"))
      (testing "with context setting middleware"
        (is (re-find
             #":m 1"
             (logging-to-string
              (with-context {:m 1}
                (log (assoc config :middleware [(add-var :context #'v)])
                     :info "something"))))
            "log message contains context")))))
