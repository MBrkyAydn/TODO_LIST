FROM eclipse-temurin:21-jre-alpine
LABEL authors="brkya"
EXPOSE 8080
COPY target/ToDo-0.0.1-SNAPSHOT.jar ToDo-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "ToDo-0.0.1-SNAPSHOT.jar"]

