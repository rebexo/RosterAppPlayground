# 1. Build Stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Kopiere Wrapper und Konfigurationsdateien (für besseres Caching)
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# Mache den Wrapper ausführbar (zur Sicherheit bei Windows-Uploads)
RUN chmod +x mvnw
# Lade Dependencies herunter (Offline-Modus vorbereiten)
RUN ./mvnw dependency:go-offline

# Kopiere den eigentlichen Quellcode
COPY src ./src

# Baue die Anwendung (überspringt Tests für schnelleres Deployment)
RUN ./mvnw clean package -DskipTests

# 2. Run Stage (Das eigentliche Image)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Kopiere das fertige JAR aus der Build-Stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]