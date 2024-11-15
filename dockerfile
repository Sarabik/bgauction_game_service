FROM gradle:7.6.0-jdk17 AS build
WORKDIR /home/app

COPY build.gradle /home/app/build.gradle
COPY settings.gradle /home/app/settings.gradle
COPY src/main/java/com/bgauction/gameservice/GameserviceApplication.java /home/app/src/main/java/com/bgauction/gameservice/GameserviceApplication.java

RUN gradle build -i --stacktrace || return 0

COPY . /home/app
RUN gradle clean build -x test

FROM openjdk:17-slim
EXPOSE 8100
ENV SPRING_PROFILES_ACTIVE=docker
COPY --from=build /home/app/build/libs/*.jar app.jar
ENTRYPOINT [ "sh", "-c", "java -jar /app.jar --spring.profiles.active=docker" ]
