FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/email-schduler-0.0.1-SNAPSHOT.jar email-schduler.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "email-schduler.jar"]