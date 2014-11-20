# Goodbot Plugins

## Development

A goodbot [plugin](src/goodbot/plugins/) is a map with the following keys.

``` clojure
{:doc {"topic" "text to display on `.help <topic>`"}
 :commands {"foo" (fn [irc message] ...) ; a callback for when .foo is entered
 :tasks [{:name "foor"
          :interval 1000 ; time between task execution
          :work (fn [irc] ...)}] ; a funtion to will be invoked periodically
```
They can define a command -> action linkage that results in a message or a task that is run periodically.


See `goodbot.plugins.ping` for a simple example of a command based plugin or `goodbot.plugins.health` for task based.
