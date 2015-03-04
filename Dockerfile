FROM dockerfile/java:oracle-java8

RUN apt-get update && apt-get install -y leiningen

#ADD sandbox.policy /usr/lib/jvm/java-7-openjdk/jre/lib/security/java.policy

WORKDIR /goodbot/

ADD . goodbot/
ENV LEIN_ROOT "true"
RUN lein install

CMD ["lein" "run"]
