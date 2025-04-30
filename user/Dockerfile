FROM gradle:jdk17 AS builder
WORKDIR /home/gradle/project

COPY settings.gradle build.gradle gradle.* ./
RUN gradle clean --no-daemon

COPY common common
COPY user    user

RUN gradle :user:bootJar --no-daemon -x test

FROM openjdk:17-jdk-slim AS runtime
WORKDIR /app

ARG JAR_FILE=home/gradle/project/user/build/libs/user-*.jar
COPY --from=builder /${JAR_FILE} app.jar

ENV SPRING_PROFILES_ACTIVE=dev
EXPOSE 8081

ENTRYPOINT ["java","-jar","app.jar"]
