FROM mysql:8.0-debian

WORKDIR /docker-entrypoint-initdb.d
RUN apt-get -y update && apt-get -y install curl \
 && curl https://javarush.com/downloads/ide/javarush/dump-hibernate-final.sql --output mysql_db.sql

ENV MYSQL_ROOT_PASSWORD=root
ENV MYSQL_DATABASE=db

EXPOSE 3306


