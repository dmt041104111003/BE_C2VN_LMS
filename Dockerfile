# Stage 1: build
# Start with a Maven image that includes JDK 21
FROM maven:3.9.8-amazoncorretto-21 AS build

# Set workdir in build container
WORKDIR /app

# Copy only pom.xml first to leverage Docker layer caching for dependencies
COPY pom.xml .

# Pre-fetch dependencies (optional but speeds up subsequent builds)
RUN mvn -q -DskipTests dependency:go-offline

# Now copy the actual source
COPY src ./src

# Build source code with maven
RUN mvn -q package -DskipTests

# Stage 2: create image
FROM amazoncorretto:21.0.4

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Expose port for Render
EXPOSE 8080

# Command to run the application with optimized JVM settings for containers
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]