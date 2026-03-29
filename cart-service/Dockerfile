FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/cart-service-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
