FROM openjdk:8-jdk-alpine

COPY target/reality.jar /reality/

WORKDIR /reality

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dtelegram.bot.name=${BOT_NAME}", "-Dtelegram.bot.token=${BOT_TOKEN}", "-Dtelegram.chat.id=${CHAT_ID}", "-Demail.host=${EMAIL_HOST}", "-Demail.port=${EMAIL_PORT}", "-Demail.login=${EMAIL_LOGIN}", "-Demail.password=${EMAIL_PASSWORD}", "reality.jar"]