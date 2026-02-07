FROM eclipse-temurin:21-jre-alpine

ARG JAR_FILE=app.jar

WORKDIR /app

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]