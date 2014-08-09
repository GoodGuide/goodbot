FROM base/devel

RUN pacman --noconfirm -Sy jdk7-openjdk

RUN pacman --noconfirm -Sy unzip

RUN curl -L https://my.datomic.com/downloads/free/0.9.4815.12 > /tmp/datomic-free.zip \
 && unzip -d /tmp /tmp/datomic-free.zip \
 && mv /tmp/datomic-free-0.9.4815.12 /app \
 && rm /tmp/datomic-free.zip

WORKDIR /app

EXPOSE 4334

ADD ./config.properties /app/config/

CMD ./bin/transactor ./config/config.properties
