FROM eclipse-temurin:17-jre

WORKDIR /app

ARG JAR_PATH=pharmacy-eureka/target
COPY ${JAR_PATH}/pharmacy-eureka-*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
