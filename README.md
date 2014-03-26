# log-config

A Clojure library designed to help configure your [timbre][timbre] logging.

The `com.palletops.log-config.timbre` namespace provides timbre
middleware and formatters.

Add `[com.palletops/log-config "0.1.0"]` to your dependencies.

## Filtering Levels by Namespace

The `min-level` function provides a timbre middleware to filter log
messages for a single namespace to those at or above a threshold
level.

The `min-levels` function provides a timbre middleware to filter log
messages based on a map from namespace to a threshold level.

The `min-level-appender` function provides an appender adaptor to
filter log messages based on a map from namespace to a threshold level
provided in the `:min-levels` timbre configuration key.

## Domain Based Logging

For logs at the domain level, the namespace is often irrelevant.  The
`with-domain` macro allows specifying a domain keyword for a dynamic
clojure scope.  The `domain-msg` timbre middleware adds this domain
keyword to log messages on the `:domain` key.  The
`format-with-domain` timbre formatter will show the domain in
preference to the namespace if the `:domain` key is set.

## Logging Contexts

Often, the same code is called in multiple contexts, and the log
message would be improved by adding some of that contextual
information.  The `with-context` macro allows specifying a data map
for a dynamic clojure scope.  The `context-msg` timbre middleware adds
this context map to log messages on the `:context` key.  The
`format-with-context` timbre formatter will show the context keys and
value in the log message.

There is also a `format-with-domain-context` that shows both domain
and context values.

## Add Log Message Key based on a Var

The `add-var` function returns a timbre middleware to set a log
message key based on the value of the specified var.

## Timbre and Java Logging

There are a number of logging choices on the JVM.  Different libraries
you use may depend on different logging libraries.  If you're writing
a library, you may wish to allow for logging via Java Logging, while
still using timbre.

### Logging From Timbre to Java Logging

The `com.palletops.log-config.timbre.tools-logging` namespace provides
the `make-tools-logging-appender` function, that returns a timbre
appender that outputs to tools.logging.

To use it, add an entry in your timbre configuration `:appenders`:

```clj
:appenders {:jl (make-tools-logging-appender {})
```

### Logging java logging to timbre

See [`taoensso.timbre.tools.logging`](http://ptaoussanis.github.io/timbre/taoensso.timbre.tools.logging.html).

## License

Copyright © 2014 Hugo Duncan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[timbre]: https://github.com/ptaoussanis/timbre
[log4j]: http://logging.apache.org/log4j/
[log4j2]: http://logging.apache.org/log4j/2.x/
[slf4j]: http://www.slf4j.org/manual.html "SLF4J"
[commons-logging]: http://commons.apache.org/proper/commons-logging "Apache Commons Logging"
