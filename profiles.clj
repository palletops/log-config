{:dev {:test-selectors {:default (complement :slf4j)
                        :slf4j :slf4j}
       :plugins [[lein-pallet-release "RELEASE"]]
       :aliases {"check" ["with-profile" "+logback" "check"]}}
 :no-checkouts {:checkout-deps-shares ^{:replace true} []}
 :logback {:dependencies [[org.clojure/tools.logging "0.2.6"]
                          [ch.qos.logback/logback-classic "1.0.9"]]
           :aliases {"test" ["test" ":slf4j"]}}
 :release {:set-version {:updates [{:path "README.md",
                                    :no-snapshot true}]}}}x
