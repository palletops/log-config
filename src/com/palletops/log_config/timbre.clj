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
(defn format-with-context
  "A formatter that appends the values in the :context key."
  [{:keys [level throwable message timestamp hostname ns] :as ev}
   ;; Any extra appender-specific opts:
   & [{:keys [nofonts?] :as appender-fmt-output-opts}]]
  (str (apply timbre/default-fmt-output-fn ev appender-fmt-output-opts)
       (if-let [context (:context ev)]
         (str " " (join ", " (map (fn [[k v]] (str k " " v)) context))))))

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
  "A formatter that show domain rather than ns when it is set."
  [{:keys [level throwable message timestamp hostname ns domain]}
   & [{:keys [nofonts?] :as appender-fmt-output-opts}]]
  ;; <timestamp> <hostname> <LEVEL> [<domain or ns>] - <message> <throwable>
  (format "%s %s %s [%s] - %s%s"
          timestamp hostname
          (-> (or domain level) name upper-case)
          ns (or message "")
          (or (timbre/stacktrace throwable "\n" (when nofonts? {})) "")))
