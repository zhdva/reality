FROM openjdk:8-jdk-alpine

COPY target/reality.jar /reality/

WORKDIR /reality

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "reality.jar"]