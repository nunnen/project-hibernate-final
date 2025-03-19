#FROM mysql:8.0-debian
#
#WORKDIR /docker-entrypoint-initdb.d
#RUN apt-get -y update && apt-get -y install curl \
# && curl https://javarush.com/downloads/ide/javarush/dump-hibernate-final.sql --output mysql_db.sql
#
#ENV MYSQL_ROOT_PASSWORD=root
#ENV MYSQL_DATABASE=db
#
#EXPOSE 3306

FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY src/ src
COPY pom.xml ./
RUN mvn package

FROM eclipse-temurin:21-jre-ubi9-minimal
WORKDIR /app
COPY --from=builder /app/target/project-hibernate-final-1.0-SNAPSHOT.jar .
CMD ["java", "-jar", "project-hibernate-final-1.0-SNAPSHOT.jar"]