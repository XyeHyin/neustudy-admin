FROM eclipse-temurin:17-jdk AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

COPY src src
RUN ./mvnw -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app

ENV APP_UPLOAD_DIR=/app/uploads

RUN mkdir -p /app/uploads
COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
