
FROM maven:3.9.12-eclipse-temurin-17

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

EXPOSE 8080

CMD ["java", "-Xms128m", "-Xmx256m", "-jar", "target/Videira-Natural-0.0.1-SNAPSHOT.jar"]
