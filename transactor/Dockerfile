FROM goodguide/base

RUN aptitude update && aptitude -y install openjdk-7-jre-headless

RUN aptitude -y install unzip
RUN wget -O /tmp/datomic-free.zip https://my.datomic.com/downloads/free/0.9.4707 \
 && unzip -d /opt /tmp/datomic-free.zip

EXPOSE 4334
CMD cd /opt/datomic-free-0.9.4707 \
 && ./bin/transactor ./config/samples/free-transactor-template.properties
