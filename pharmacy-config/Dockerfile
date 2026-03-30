FROM eclipse-temurin:17-jre

WORKDIR /app

ARG JAR_PATH=pharmacy-config/target
COPY ${JAR_PATH}/pharmacy-config-*.jar app.jar
COPY pharmacy-config/config-repo /config-repo

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
