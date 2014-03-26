(ns com.palletops.log-config.tools-logging
  (:require
   [clojure.stacktrace :as stacktrace]
   [clojure.tools.logging :as logging]))

;; tools.logging 0.2.0 and up

;;; A null logger
;;; Suppresses all logging.  Can be useful to quiet test cases.
(deftype NullLogger
    []
  clojure.tools.logging.impl.Logger
  (enabled? [log level] false)
  (write! [log level throwable message]))

(def null-log (delay (NullLogger.)))

(deftype NullLoggerFactory
    []
  clojure.tools.logging.impl.LoggerFactory
  (name [factory] "null logger")
  (get-logger [factory log-ns] @null-log))

(def null-logger-factory (delay (NullLoggerFactory.)))

;;; A stdout logger
;;; Logs everyting to stdout.  Can be useful to test logging.
(deftype StdoutLogger
    []
  clojure.tools.logging.impl.Logger
  (enabled? [log level] true)
  (write! [log level throwable message]
    (println (name level) message)
    (when throwable
      (stacktrace/print-stack-trace
       (stacktrace/root-cause throwable)))))
(def stdout-log (delay (StdoutLogger.)))

(deftype StdoutLoggerFactory
    []
  clojure.tools.logging.impl.LoggerFactory
  (name [factory] "stdout logger")
  (get-logger [factory log-ns] @stdout-log))
(def stdout-logger-factory (delay (StdoutLoggerFactory.)))

(defmacro with-logger-factory
  [factory & body]
  `(binding [logging/*logger-factory* ~factory] ~@body))

;;; Macros to use specific logging implementations in a given scope
(defmacro logging-to-stdout
  "Send log messages to stdout for inspection"
  [& forms]
  `(with-logger-factory @stdout-logger-factory
     ~@forms))

(defmacro logging-to-string
  "Send log messages to a string for inspection"
  [& forms]
  `(with-out-str
     (with-logger-factory @stdout-logger-factory
       ~@forms)))

(defmacro suppress-logging
  "Prevent log messages to reduce test log noise"
  [& forms]
  `(with-logger-factory @null-logger-factory
     ~@forms))
