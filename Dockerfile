# Use buildx for multi-architecture builds
FROM --platform=$BUILDPLATFORM eclipse-temurin:17-jdk as build
WORKDIR /workspace/app

# Copy maven files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Fix permissions and download dependencies
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -B

# Copy source and build
COPY src src
RUN ./mvnw package -DskipTests

# Use a smaller JRE image for runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]