FROM goodguide/base

RUN aptitude update && aptitude -y install openjdk-7-jre-headless

ENV LEIN_ROOT 1
RUN wget https://raw.github.com/technomancy/leiningen/stable/bin/lein -O /usr/local/bin/lein \
 && chmod +x /usr/local/bin/lein \
 && lein

ADD . /app
RUN cd /app && lein deps
RUN cp /app/sandbox.policy /usr/lib/jvm/java-7-openjdk-amd64/jre/lib/security/java.policy
CMD cd /app && lein run