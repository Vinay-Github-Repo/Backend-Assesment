# --- Stage 1: Build the application ---
FROM maven:3.8.8-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -B -DskipTests package

# --- Stage 2: Create the final, smaller runtime image ---
# Changed the base image to the official Eclipse Temurin JRE (Java Runtime Environment)
# '21-jre-jammy' is a common, secure, and small base for Java 21 apps.
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /workspace/target/backend-assessment-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]