{:dev {:test-selectors {:default (complement :slf4j)
                        :slf4j :slf4j}}
 :logback {:dependencies [[org.clojure/tools.logging "0.2.6"]
                          [ch.qos.logback/logback-classic "1.0.9"]]
           :aliases {"test" ["test" ":slf4j"]}}}
