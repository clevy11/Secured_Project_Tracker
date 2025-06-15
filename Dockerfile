# Build stage - just copy the JAR
FROM eclipse-temurin:17-jre-alpine as builder
WORKDIR /app
COPY target/*.jar app.jar

# Extract the JAR to optimize layers
RUN java -Djarmode=layertools -jar app.jar extract

# Final run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the extracted JAR layers
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

# Set timezone
RUN apk --no-cache add tzdata && \
    cp /usr/share/zoneinfo/Europe/Paris /etc/localtime && \
    echo "Europe/Paris" > /etc/timezone && \
    apk del tzdata

# Add non-root user
RUN addgroup -S spring && \
    adduser -S spring -G spring && \
    chown -R spring:spring /app

USER spring:spring

EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
