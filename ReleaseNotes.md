## 0.1.3

- Add dynamic wrapping of vars
  This lets you dynamically add and remove entry and exit logging to all 
  functions in a namespace.

- Add :kws option to async-appender
  Allow specification of the keys to forward using the :kws key. Defaults to
  [:hostname :ns :args :throwable :profile-stats].

- Add async-channel appender and memory-sink
  Add an appender that writes to a core.async channel.  The memory-sink can
  be used to read the channel into an atom with bounded memory usage.

  Closes #3

## 0.1.2

- Add with-tags, tags-msg to add a tag set to :tags
  Adds the with-tags macro to set tags for a scope.  The tags-msg middleware
  is used to add the tags onto the :tags message key.

  The :tags are not logged (by default).

  Closes #2

## 0.1.1

- Fix with-total-unquote
  Fixes #1

## 0.1.0

- Initial Release
