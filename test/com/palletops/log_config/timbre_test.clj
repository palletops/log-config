(ns com.palletops.log-config.timbre-test
  (:require
   [clojure.test :refer :all]
   [taoensso.timbre :refer [example-config info log]]
   [com.palletops.log-config.timbre
    :refer [add-var format-with-context]]))

(def v {:m 1})

(deftest context-test
  (is (re-find #".*:m 1\n$"
               (with-out-str
                 (log (merge example-config
                             {:fmt-output-fn format-with-context
                              :middleware [(add-var :context #'v)]})
                      :info "something")))))
