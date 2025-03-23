FROM amazoncorretto:17-alpine

WORKDIR /app

COPY sky-server/target/sky-server-1.0-SNAPSHOT.jar /app/lib/

CMD ["java", "-jar", "/app/lib/sky-server-1.0-SNAPSHOT.jar"]

EXPOSE 8080
