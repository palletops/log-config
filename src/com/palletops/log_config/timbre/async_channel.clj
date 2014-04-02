(ns com.palletops.log-config.timbre.async-channel
  "A timbre appender that writes to a channel"
  (:require
   [clojure.core.async :refer [go put!]]))

(defn make-async-channel-appender
  "Returns an async channel appender, the outputs to ch.

  It is advised that the channel, ch, is non-blocking.

  A config map can be provided here as an argument, or as
  an :async-channel key in :shared-appender-config.

  (make-async-channel-appender {:enabled? true})"
  [ch appender-opts]
  (let [default-opts {:enabled? true}]
    (merge default-opts
           appender-opts
           {:fn
            (fn [{:keys [] :as apfn-args}]
              (put! ch (select-keys apfn-args [:hostname :ns :args :throwable
                                               :profile-stats])))})))

(defn- add-to-memory
  "Add `msg` to the sequence in the atom `memory`, limiting the
  sequence to `n` elements."
  [memory n msg]
  (if (>= (count @memory) n)
    (swap! memory (fn [s] (conj (pop s) msg)))
    (swap! memory (fnil conj []) msg)))

(defn memory-sink
  "Save messages from an async channel, `ch`, into a bounded memory area,
  held in the atom, `memory`.  Save up to `max-messages` messages."
  [ch memory max-messages]
  {:pre [(instance? clojure.lang.Atom memory)
         (number? max-messages)]}
  (go
    (loop []
      (if-let [m (<! ch)]
        (do
          (add-to-memory memory max-messages m)
          (recur))
        @memory))))
