# goodbot

GoodGuide's IRC bot

## Usage

Type `.help` in IRC.

## Development

This project is set up with leiningen.

A goodbot plugin is a map with the following keys

``` clojure
{:command "foo" ; the command used to call this plugin, in this case .foo
 :doc ".foo <arg> <arg> : a docstring that will be printed on `.help foo`"
 :handler (fn [irc message] ...)} ; a callback for when this plugin is called
```

See `goodbot.plugins.ping` for a simple example.
