package org.zhadaev.reality;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.multipart.MultipartBody;
import jakarta.inject.Singleton;

import java.io.InputStream;

@Singleton
public class TelegramSender {

    private final HttpClient httpClient;
    private final String botToken;
    private final String chatId;

    public TelegramSender(@Client("https://api.telegram.org") HttpClient httpClient,
                          @Value("${telegram.bot-token}") String botToken,
                          @Value("${telegram.chat-id}") String chatId) {
        this.httpClient = httpClient;
        this.botToken = botToken;
        this.chatId = chatId;
    }

    public void sendPhoto(String fileName, InputStream photo, long contentLength) {
        MultipartBody requestBody = MultipartBody.builder()
                                                 .addPart("chat_id", chatId)
                                                 .addPart("photo", fileName, photo, contentLength)
                                                 .build();
        httpClient.toBlocking().exchange(
                HttpRequest.POST("/bot" + botToken + "/sendPhoto", requestBody)
                        .contentType(MediaType.MULTIPART_FORM_DATA_TYPE)
        );
    }

}
