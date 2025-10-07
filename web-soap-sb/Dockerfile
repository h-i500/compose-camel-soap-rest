FROM eclipse-temurin:17-jre AS base
WORKDIR /app

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -e -U -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM base
COPY --from=build /src/target/web-soap-sb-*.jar /app/app.jar
EXPOSE 8089
ENV SERVER_PORT=8089
ENTRYPOINT ["java","-jar","/app/app.jar"]
