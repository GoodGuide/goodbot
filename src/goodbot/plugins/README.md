# Goodbot Plugins

All plugins in this directory will be loaded into Goodbot on startup.

## Development

A goodbot [plugin](src/goodbot/plugins/) is a map with the following keys.

``` clojure
{:doc {"topic" "text to display on `.help <topic>`"}
 :commands {"foo" (fn [irc message] ...)} ; a callback for when .foo is entered
```
They define a command -> action linkage that results in a message.


See `goodbot.plugins.ping` for a simple example.
