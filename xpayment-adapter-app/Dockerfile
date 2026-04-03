FROM amazoncorretto:21.0.10

WORKDIR /app

COPY target/xpayment-adapter-app-0.0.1-SNAPSHOT.jar /app/xpayment-adapter-app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/xpayment-adapter-app.jar"]
