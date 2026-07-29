FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY common-lib/pom.xml common-lib/
COPY recruiter-verification-service/pom.xml recruiter-verification-service/
RUN mvn dependency:go-offline -pl common-lib,recruiter-verification-service -am -q
COPY common-lib/src common-lib/src
COPY recruiter-verification-service/src recruiter-verification-service/src
RUN mvn package -pl common-lib,recruiter-verification-service -am -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/recruiter-verification-service/target/recruiter-verification-service-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
