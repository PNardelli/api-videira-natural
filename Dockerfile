FROM maven:3.9.12-eclipse-temurin-17 AS build

WORKDIR /app

# Baixa as dependências primeiro para aproveitar cache quando o pom.xml não mudar
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

# Agora copia o código e empacota usando o cache local
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/Videira-Natural-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]