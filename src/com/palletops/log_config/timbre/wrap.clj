(ns com.palletops.log-config.timbre.wrap
  "Add logging and profiling to your functions dynamically."
  (:require
   [taoensso.timbre :refer [log logf]]))

(defn wrap
  "Wrap a function with a set of function wrappers."
  [f f-info [wrapper & wrappers]]
  (if (seq wrappers)
    (wrap (wrapper f f-info) f-info wrappers)
    (wrapper f f-info)))

(defn wrap-var
  "Add a function wrapper to a Var, v, if it is a plain function.
  The wrapping can be undone by calling unwrap-var."
  [v wrappers]
  {:pre [(var? v)]}
  (let [f @v
        f-info (meta v)]
    (when (and (fn? f) (not (:macro f-info)) (not (::original f-info)))
      (doto v
        (alter-meta! assoc ::original f)
        (alter-var-root wrap f-info wrappers)))))

(defn unwrap-var
  "Remove function wrappers from a Var, v."
  [v]
  {:pre [(var? v)]}
  (when-let [f (::original (meta v))]
    (doto v
      (alter-var-root (constantly f))
      (alter-meta! dissoc ::original))))

(defn wrap-ns-vars
  "Replaces each function from the given namespace with a wrapped version.
  Can be undone with wrap-ns-vars. ns should be a namespace
  object or a symbol. "
  [ns wrappers]
  (let [ns-fn-vars (->> ns ns-interns vals (filter (comp fn? var-get)))]
    (doseq [v ns-fn-vars]
      (wrap-var v wrappers))))

(defn unwrap-ns-vars
  "Remove function wrappers from the functions in a given namespace."
  [ns]
  (let [ns-fn-vars (->> ns ns-interns vals (filter (comp fn? var-get)))]
    (doseq [v ns-fn-vars]
      (unwrap-var v))))

(defn fn-entry-logger
  "Return a wrapper function to wrap functions with entry logging at
  the specified log level."
  [level]
  (fn [f {:keys [name]}]
    (fn entry-logger [& args]
      (log level name args)
      (apply f args))))

(defn fn-exit-logger
  "Return a wrapper function to wrap functions with exit logging at
  the specified log level."
  [level]
  (fn [f {:keys [name]}]
    (fn entry-logger [& args]
      (let [r (apply f args)]
        (log level name "->" r)
        r))))

(defn log-ns-fns
  "Log entry and exit of all functions in a namespace."
  [ns level]
  (wrap-ns-vars ns [(fn-entry-logger level)(fn-exit-logger level)]))
