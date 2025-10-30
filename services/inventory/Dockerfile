FROM eclipse-mosquitto:21-jre AS runtime

WORKDIR /app

ARG JAR=target/inventory-0.0.1-SNAPSHOT.jar

COPY ${JAR} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=docker"]