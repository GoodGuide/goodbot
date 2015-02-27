FROM dockerfile/java:oracle-java8

ENV DATOMIC_VERSION 0.9.5067

RUN curl --progress-bar --location --user-agent 'tauhoDB (info@tauho.db)' --url "https://my.datomic.com/downloads/free/${DATOMIC_VERSION}" --output datomic.zip \
  && unzip datomic.zip \
  && rm datomic.zip \
  && mv datomic-free-$DATOMIC_VERSION datomic \
  && cd datomic

ENV PATH $PATH:/data/datomic/bin

ADD free-transactor.properties /data/datomic/

EXPOSE 4334 4335

CMD ["transactor", "/data/datomic/free-transactor.properties"]
