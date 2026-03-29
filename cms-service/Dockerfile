FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/cms-service-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
