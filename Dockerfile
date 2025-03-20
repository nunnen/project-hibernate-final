#FROM maven:3.9.6-eclipse-temurin-21 AS builder
#WORKDIR /app
#COPY src/ src
#COPY pom.xml ./
#RUN mvn clean package
#
#FROM eclipse-temurin:21-jdk
#WORKDIR /app
#COPY --from=builder /app/target/project-hibernate-final-1.0-SNAPSHOT.jar hibernate-final.jar
#CMD ["java", "-jar", "hibernate-final.jar"]

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY target/project-hibernate-final-1.0-SNAPSHOT.jar app.jar
CMD ["sh", "-c", "sleep 10 && java -jar app.jar"]