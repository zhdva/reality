FROM amazoncorretto:8-alpine-jdk

COPY target/reality.jar /reality/
COPY tinyproxy.conf /tinyproxy/

RUN apk add --no-cache tinyproxy

EXPOSE 8080 8888

CMD ["sh", "-c", "tinyproxy -d -c /tinyproxy/tinyproxy.conf & java -jar /reality/reality.jar"]