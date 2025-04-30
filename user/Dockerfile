FROM gradle:8-jdk17-alpine AS builder
WORKDIR /home/gradle/project

COPY settings.gradle build.gradle gradle.* ./
RUN gradle clean --no-daemon

COPY common common
COPY user    user

RUN gradle :user:bootJar --no-daemon -x test

FROM eclipse-temurin:17-jdk-slim
WORKDIR /app

ARG JAR_FILE=home/gradle/project/user/build/libs/user-*.jar
COPY --from=builder /${JAR_FILE} app.jar

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8081

ENTRYPOINT ["java","-jar","app.jar"]
