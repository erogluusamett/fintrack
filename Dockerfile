# --- Build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Bağımlılıkları önce indir (kaynak değişmediği sürece Docker layer cache'inden yararlanır)
COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Container root olarak çalışmasın diye ayrı bir kullanıcı
RUN addgroup -S fintrack && adduser -S fintrack -G fintrack
COPY --from=build /build/target/fintrack-*.jar app.jar
USER fintrack

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
