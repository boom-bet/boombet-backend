FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN ./mvnw dependency:go-offline
CMD ["./mvnw", "spring-boot:run"]
