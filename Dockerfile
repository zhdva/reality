FROM amazoncorretto:25-alpine-jdk

COPY target/reality.jar /reality/
COPY entrypoint.sh /entrypoint.sh

RUN apk add --no-cache tailscale curl
RUN chmod +x /entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/entrypoint.sh"]