FROM base/devel

RUN pacman --noconfirm -Sy jdk7-openjdk
RUN pacman --noconfirm -Sy socat

ENV LEIN_ROOT 1
RUN curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > /usr/local/bin/lein \
 && chmod +x /usr/local/bin/lein \
 && lein

WORKDIR /app

ADD project.clj /app/
RUN lein deps

ADD sandbox.policy /usr/lib/jvm/java-7-openjdk/jre/lib/security/java.policy

ADD src /app/src
RUN lein compile

ADD resources /app/resources

CMD socat TCP-LISTEN:4334,fork TCP:$TRANSACTOR_PORT_4334_TCP_ADDR:4334 & \
    cd /app && lein run
