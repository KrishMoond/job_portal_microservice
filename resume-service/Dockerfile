FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY common-lib/pom.xml common-lib/
COPY config-server/pom.xml config-server/
COPY eureka-server/pom.xml eureka-server/
COPY api-gateway/pom.xml api-gateway/
COPY user-service/pom.xml user-service/
COPY job-service/pom.xml job-service/
COPY application-service/pom.xml application-service/
COPY resume-service/pom.xml resume-service/
COPY search-service/pom.xml search-service/
COPY notification-service/pom.xml notification-service/
COPY analytics-service/pom.xml analytics-service/
RUN mvn dependency:go-offline -pl common-lib,resume-service -am -q
COPY common-lib/src common-lib/src
COPY resume-service/src resume-service/src
RUN mvn package -pl common-lib,resume-service -am -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/resume-service/target/resume-service-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
