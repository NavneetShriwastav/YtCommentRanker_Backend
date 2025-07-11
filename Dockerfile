# Use Java 21 base image
FROM eclipse-temurin:21

# Set workdir
WORKDIR /app

# Copy everything from your project to the container
COPY . .

# Make sure Maven Wrapper is executable
RUN chmod +x mvnw

# Build the project
RUN ./mvnw clean package

# Expose Spring Boot default port
EXPOSE 8080

# Run the built JAR
CMD ["java", "-jar", "target/YTCommentRanker-0.0.1-SNAPSHOT.jar"]
