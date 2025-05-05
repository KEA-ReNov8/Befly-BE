FROM openjdk:17-jdk-slim
WORKDIR /app

ARG JAR_FILE=build/libs/consult-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

ENV SPRING_PROFILES_ACTIVE=dev
ENV SERVER_PORT=8082
# 포트 명시
EXPOSE 8082

ENTRYPOINT ["java","-jar","app.jar"]
