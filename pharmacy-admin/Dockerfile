FROM eclipse-temurin:17-jre

WORKDIR /app

ARG JAR_PATH=pharmacy-admin/target
COPY ${JAR_PATH}/pharmacy-admin-*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
