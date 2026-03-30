FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /build

COPY app/pom.xml .
RUN mvn dependency:go-offline

COPY app/src ./src
RUN mvn clean package -DskipTests

# ---- Runtime Stage ----
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=builder /build/target/eks-java-app-1.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
