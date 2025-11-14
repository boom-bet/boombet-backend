FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN ./mvnw -Dhttps.protocols=TLSv1.2 dependency:go-offline
CMD ["./mvnw", "spring-boot:run"]
