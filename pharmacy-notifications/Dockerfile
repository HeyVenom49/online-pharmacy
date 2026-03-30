FROM eclipse-temurin:17-jre

WORKDIR /app

ARG JAR_PATH=pharmacy-notifications/target
COPY ${JAR_PATH}/pharmacy-notifications-*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
