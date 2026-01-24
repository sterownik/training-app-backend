# Oficjalny obraz OpenJDK 21 z Mavena
FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /app

# Skopiuj pliki Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw

# Skopiuj źródła
COPY src src

# Build JAR
RUN ./mvnw clean package -DskipTests

# Stage do uruchomienia
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
