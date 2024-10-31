FROM gradle:jdk23 AS build
COPY --chown=gradle:gradle . /src
WORKDIR /src
RUN ./gradlew bootJar

FROM openjdk:23-jdk as runtime
WORKDIR /app
COPY --from=build /src/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]