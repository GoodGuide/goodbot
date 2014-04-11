FROM goodguide/base

RUN aptitude update && aptitude -y install openjdk-7-jre-headless

RUN aptitude -y install unzip

RUN wget -O /tmp/datomic-free.zip https://my.datomic.com/downloads/free/0.9.4707 \
 && unzip -d /tmp /tmp/datomic-free.zip \
 && mv /tmp/datomic-free-0.9.4707 /app \
 && rm /tmp/datomic-free.zip

ADD ./config.properties /app/config/config.properties

EXPOSE 4334
CMD cd /app && ./bin/transactor ./config/config.properties
