(defproject com.palletops/log-config "0.1.4"
  :description "Log appenders and middleware for timbre"
  :url "http://github.com/palletops/log-config"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"
                  :scope "provided"]
                 [org.clojure/tools.logging "0.2.6"]
                 [com.taoensso/timbre "3.1.6"]
                 [org.clojure/core.async "0.1.278.0-76b25b-alpha"
                  :scope "provided"]])
