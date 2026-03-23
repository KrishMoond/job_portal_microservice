FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/search-service-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
