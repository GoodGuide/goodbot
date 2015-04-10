FROM dockerfile/java:oracle-java8

RUN apt-get update && apt-get install -y leiningen

ADD sandbox.policy /usr/lib/jvm/java-8-oracle/jre/lib/security/java.policy

WORKDIR /goodbot/

ENV LEIN_ROOT "true"
ADD . /goodbot/
RUN lein install

ENTRYPOINT ["script/docker_wrapper"]

CMD ["lein", "run"]