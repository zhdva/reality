FROM eclipse-temurin:25-jre-alpine

COPY target/reality.jar /reality/
COPY /scripts /scripts

RUN chmod +x /scripts/entrypoint.sh

EXPOSE 8080 8443

ENTRYPOINT ["/scripts/entrypoint.sh"]