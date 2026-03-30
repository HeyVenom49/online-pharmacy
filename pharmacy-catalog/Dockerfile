FROM eclipse-temurin:17-jre

WORKDIR /app

ARG JAR_PATH=pharmacy-catalog/target
COPY ${JAR_PATH}/pharmacy-catalog-*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
