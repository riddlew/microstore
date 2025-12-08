# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper files
COPY mvnw ./
COPY .mvn .mvn

# Make mvnw executable
RUN chmod +x mvnw

# Copy pom.xml and download dependencies (this layer will be cached)
COPY pom.xml ./
RUN ./mvnw dependency:go-offline -B || true

# Copy source code
COPY src ./src

# Build the application (MapStruct will generate the mapper implementation here)
RUN ./mvnw clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
