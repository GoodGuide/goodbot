# goodbot

GoodGuide's IRC bot which connects to Slack via the IRC gatway.

## Usage

Type `.help` in IRC.

## Development

This project is set up with leiningen.

To get started on OSX:
``` shell
  brew install leiningen
  git clone https://github.com/GoodGuide/goodbot.git
  cd goodbot
  lein run
```

Goodbots can be extended via simple [plugins](src/goodbot/plugins/).
