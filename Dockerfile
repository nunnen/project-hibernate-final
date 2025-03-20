FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY src/ src
COPY pom.xml ./
RUN mvn package

FROM eclipse-temurin:21-jre-ubi9-minimal
WORKDIR /app
COPY --from=builder /app/target/project-hibernate-final-1.0-SNAPSHOT-jar-with-dependencies.jar project-hibernate-final.jar
CMD ["java", "-jar", "project-hibernate-final.jar"]