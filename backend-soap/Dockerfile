FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -e -B dependency:go-offline
COPY src ./src
RUN mvn -q -e -B package -DskipTests

FROM eclipse-temurin:21-jre
ENV TZ=Asia/Tokyo JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -Duser.timezone=Asia/Tokyo"
WORKDIR /opt/app
COPY --from=build /workspace/target/backend-soap.jar app.jar
# Narayana ObjectStore を永続化
VOLUME ["/opt/app/objectstore"]
EXPOSE 8080
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
