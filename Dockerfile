FROM amazoncorretto:25-alpine-jdk

COPY target/reality.jar /reality/
COPY /scripts /scripts

RUN chmod +x /scripts/entrypoint.sh

EXPOSE 8080 8443

ENTRYPOINT ["/scripts/entrypoint.sh"]