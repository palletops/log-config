(ns com.palletops.log-config.timbre.tools-logging
  "An appender for timbre that logs to tools.logging."
  (:require
   [clojure.java.io :as io]
   [clojure.tools.logging :as logging]
   [taoensso.timbre :refer [info]]))

(def has-slf4j?
  (delay
   (if-let [x (try (import org.slf4j.Logger)
                   (catch ClassNotFoundException _))]
     true
     (info
      "MDC Logging contexts are not supported by your logger configuration"))))

(defmacro with-context
  "Specify the logging context for a given `body`. `bindings` is a vector of
   keyword value pairs to be set on the Mapped Diagnostic Context. If the
   current logger doesn't support contexts, then the body is just wrapped in a
   `do`. slf4j is the only supported logger at present."
  [kw-vals & body]
  (if @has-slf4j?
    (do
      (require 'com.palletops.log-config.slf4j)
      `(com.palletops.log-config.slf4j/with-context ~kw-vals
         ~@body))
    `(do ~@body)))

(defn- appender-fn [{:keys [ap-config ns level instant throwable message
                            output context]}]
  (let [{:keys [] :or {}} (:tools-logging ap-config)]
    (if context
      (with-context context
        (logging/log ns level throwable output))
      (logging/log ns level throwable output))))

(defn make-tools-logging-appender
  "Returns a tools.logging appender.
  A config map can be provided here as an argument, or as a :tools-logging key
  in :shared-appender-config.

  (make-tools-logging-appender {:enabled? true})"
  [appender-opts]
  (let [default-opts {:enabled? true}]
    (merge default-opts
           appender-opts
           {:fn appender-fn})))
