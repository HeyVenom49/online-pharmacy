FROM eclipse-temurin:17-jre

WORKDIR /app

ARG JAR_PATH=pharmacy-gateway/target
COPY ${JAR_PATH}/pharmacy-gateway-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
