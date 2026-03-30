FROM eclipse-temurin:17-jre

WORKDIR /app

ARG JAR_PATH=pharmacy-orders/target
COPY ${JAR_PATH}/pharmacy-orders-*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
