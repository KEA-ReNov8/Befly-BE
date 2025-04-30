FROM gradle:jdk17 AS builder
WORKDIR /home/gradle/project

COPY settings.gradle build.gradle gradle.* ./
RUN gradle clean --no-daemon

COPY common common
COPY community community

RUN gradle :community:bootJar --no-daemon -x test

FROM openjdk:17-jdk-slim AS runtime
WORKDIR /app

ARG JAR_FILE=home/gradle/project/community/build/libs/community-*.jar
COPY --from=builder /${JAR_FILE} app.jar

ENV SPRING_PROFILES_ACTIVE=dev
ENV SERVER_PORT=8083

EXPOSE 8083

ENTRYPOINT ["java","-jar","app.jar"]
