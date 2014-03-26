(ns com.palletops.log-config.timbre
  "Timbre middleware functions"
  (:require
   [clojure.string :refer [join upper-case]]
   [taoensso.timbre :as timbre]))

;;; # Namespace Specific Level Filtering
(defn min-level
  "Returns a timbre middleware to filter messages for namespace by min
  level."
  [ns level]
  (fn min-level [msg]
    (if (or (not= ns (:ns msg))
            (<= 0 (#'timbre/levels-compare (:level msg) level)))
      msg)))

(defn min-levels
  "Returns a timbre middleware to filter messages for namespace by a
  map of min level for each ns."
  [level-map]
  (fn min-levels [msg]
    (if-let [level (get level-map (:ns msg))]
      (if (<= 0 (#'timbre/levels-compare (:level msg) level))
        msg)
      msg)))

(defn min-level-appender
  "Returns a timbre appender that wraps the specified appender and
  filters messages for namespace by a map of min level for each ns,
  given in the :min-levels config key."
  [appender]
  (fn min-level-appender [{:keys [ap-config ns level] :as msg}]
    (if-let [min-lvl (level (get (:min-levels ap-config {}) (:ns msg)))]
      (if (<= 0 (#'timbre/levels-compare level min-lvl))
        (appender msg))
      (appender msg))))

;;; # Add Log Message Key based on a Var
(defn add-var
  "Return a timbre middleware to add the value of the given var as a
  kw in the message map."
  [kw v]
  {:pre [(var? v)]}
  (fn add-var
    [msg]
    (assoc msg kw (var-get v))))


;;; # Context in Log Messages
(def ^{:dynamic true :doc "Thread specific context"}
  *context* nil)

(defn context
  "Return the current context."
  []
  *context*)

(defmacro with-total-context
  "Execute body within the given context."
  [context & body]
  `(binding [*context* context]
     ~@body))

(defmacro with-context
  "Execute body with the given context merged onto the current context."
  [context & body]
  `(binding [*context* (merge *context* ~context)]
     ~@body))

(defmacro with-context-update
  "Execute body with the given context merged onto the current context."
  [[path f & args] & body]
  `(binding [*context* (update-in *context* ~path ~f ~@args)]
     ~@body))

(def context-msg
  "Add context to log messages on the :context key"
  (add-var :context #'*context*))

(defn format-with-context
  "A formatter that shows values in the :context key."
  [{:keys [level throwable message timestamp hostname ns context] :as ev}
   ;; Any extra appender-specific opts:
   & [{:keys [nofonts?] :as appender-fmt-output-opts}]]
  (format "%s %s %s [%s]%s - %s%s"
          timestamp hostname
          (-> level name upper-case)
          (if (seq context)
            (str " " (join " " (map (fn [[k v]] (str k " " v)) context)))
            "")
          ns (or message "")
          (or (timbre/stacktrace throwable "\n" (when nofonts? {})) "")))

;;; # Domain logging

;;; Domain logging is not tied to namespaces.
(def ^:dynamic *domain* nil)

(defmacro with-domain
  "Set the domain for any log messages in body."
  [domain & body]
  `(binding [*domain* ~domain]
     ~@body))

(def domain-msg
  "Add domain to log messages on the :domain key"
  (add-var :domain #'*domain*))

(defn format-with-domain
  "A formatter that shows domain rather than ns when it is set."
  [{:keys [level throwable message timestamp hostname ns domain]}
   & [{:keys [nofonts?] :as appender-fmt-output-opts}]]
  ;; <timestamp> <hostname> <LEVEL> [<domain or ns>] - <message> <throwable>
  (format "%s %s %s [%s] - %s%s"
          timestamp hostname
          (-> level name upper-case)
          (or (and domain (name domain)) ns)
          (or message "")
          (or (timbre/stacktrace throwable "\n" (when nofonts? {})) "")))

;;; # Formatter for Context and Domain
(defn format-with-domain-context
  "A formatter that shows domain rather than ns when it is set, and
  adds any :context values."
  [{:keys [level throwable message timestamp hostname ns domain context]}
   & [{:keys [nofonts?] :as appender-fmt-output-opts}]]
  ;; <timestamp> <hostname> <LEVEL> [<domain or ns>] - <message> <throwable>
  (format "%s %s %s [%s]%s - %s%s"
          timestamp hostname
          (-> level name upper-case)
          (or (and domain (name domain)) ns)
          (if (seq context)
            (str " " (join " " (map (fn [[k v]] (str k " " v)) context)))
            "")
          (or message "")
          (or (timbre/stacktrace throwable "\n" (when nofonts? {})) "")))

;;; # Logging Threshold Fixture

(defn logging-threshold-fixture
  "Change the logging threshold inside a scope."
  ([level appender]
     (fn [f]
       (let [config @timbre/config]
         (timbre/set-config! [:appenders appender :min-level] level)
         (try (f)
              (finally (timbre/merge-config! config))))))
  ([level] (logging-threshold-fixture level :standard-out))
  ([] (logging-threshold-fixture :warn :standard-out)))

(defmacro suppress-logging
  "Suppress all logging inside the scope of the function."
  [& body]
  `(let [config# @timbre/config]
     (try
       (doseq [appender# (:keys (:appenders config#))]
         (timbre/set-config! [:appenders appender# :enabled?] false))
       ~@body
       (finally (timbre/merge-config! config#)))))
