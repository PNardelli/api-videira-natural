FROM maven:3.9.12-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B || true
COPY . .
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/Videira-Natural-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]