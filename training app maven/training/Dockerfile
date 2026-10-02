# Stage 1: build frontendu (Angular) prosto z repozytorium na GitHubie
FROM node:22-alpine AS frontend

ARG FRONTEND_REPO=sterownik/training-app-frontend
ARG FRONTEND_BRANCH=main

RUN apk add --no-cache git

WORKDIR /frontend

# Zmienia sie przy kazdym nowym commicie we froncie, wiec Docker nie uzyje starej wersji z cache
ADD https://api.github.com/repos/${FRONTEND_REPO}/git/refs/heads/${FRONTEND_BRANCH} /tmp/frontend-version.json

RUN git clone --depth 1 --branch ${FRONTEND_BRANCH} https://github.com/${FRONTEND_REPO}.git . \
    && npm ci \
    && npx ng build

# Stage 2: build backendu z Mavena
FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /app

# Skopiuj pliki Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw

# Skopiuj źródła
COPY src src

# Wrzuć zbudowany frontend do zasobów statycznych Springa
RUN rm -rf src/main/resources/static
COPY --from=frontend /frontend/dist/training-app/browser src/main/resources/static

# Build JAR
RUN ./mvnw clean package -DskipTests

# Stage 3: uruchomienie
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
