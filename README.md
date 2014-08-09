# goodbot

GoodGuide's IRC bot

## Usage

Type `.help` in IRC.

## Development

This project is set up with leiningen.

A goodbot plugin is a map with the following keys

``` clojure
{:doc {"topic" "text to display on `.help <topic>`"}
 :commands {"foo" (fn [irc message] ...)} ; a callback for when .foo is entered
```

See `goodbot.plugins.ping` for a simple example.
