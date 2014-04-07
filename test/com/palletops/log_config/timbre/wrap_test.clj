(ns com.palletops.log-config.timbre.wrap-test
  (:require
   [clojure.test :refer :all]
   [com.palletops.log-config.timbre.wrap :refer :all]))

(defn test-fn [x] (str x x))

(deftest wrap-unwrap-test
  (try
    (log-ns-fns 'com.palletops.log-config.timbre.wrap-test :info)
    (is (re-find #"(?s).*test-fn \(:a\).*test-fn -> :a:a.*"
                 (with-out-str (test-fn :a))))
    (finally
      (unwrap-ns-vars 'com.palletops.log-config.timbre.wrap-test)))
  (is (= "" (with-out-str (test-fn :a)))))
