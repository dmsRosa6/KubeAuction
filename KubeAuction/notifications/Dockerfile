FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY target/auction-worker.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

