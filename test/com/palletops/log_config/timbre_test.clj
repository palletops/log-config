(ns com.palletops.log-config.timbre-test
  (:require
   [clojure.test :refer :all]
   [taoensso.timbre :refer [example-config info log]]
   [com.palletops.log-config.timbre
    :refer [add-var context-msg domain-msg format-with-context
            format-with-domain format-with-domain-context
            with-domain with-context]]))

(def v {:m 1})

(deftest context-test
  (testing "context via explicit add-var"
    (is (re-find #":m 1"
                 (with-out-str
                   (log (merge example-config
                               {:fmt-output-fn format-with-context
                                :middleware [(add-var :context #'v)]})
                        :info "something")))))
  (testing "context via with-context and context-msg"
    (is (re-find #":m 1"
                 (with-out-str
                   (with-context {:m 1}
                     (log (merge example-config
                                 {:fmt-output-fn format-with-context
                                  :middleware [context-msg]})
                          :info "something")))))))

(def d :domain)

(deftest domain-test
  (testing "domain via explicit add-var"
    (is (re-find #"\[domain\]"
                 (with-out-str
                   (log (merge example-config
                               {:fmt-output-fn format-with-domain
                                :middleware [(add-var :domain #'d)]})
                        :info "something")))))
  (testing "context via with-domain and domain-msg"
    (is (re-find #"\[domain\]"
                 (with-out-str
                   (with-domain :domain
                     (log (merge example-config
                                 {:fmt-output-fn format-with-domain
                                  :middleware [domain-msg]})
                          :info "something")))))))

(deftest domain-context-test
  (testing "domain and context"
    (is (re-find #"\[domain\] :m 1"
                 (with-out-str
                   (with-domain :domain
                     (with-context {:m 1}
                       (log (merge example-config
                                   {:fmt-output-fn format-with-domain-context
                                    :middleware [context-msg domain-msg]})
                            :info "something"))))))))
