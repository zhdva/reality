package org.zhadaev.reality;

import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Singleton
public class TelegramSender {

    private final HttpClient httpClient;
    private final String botToken;
    private final String chatId;

    public TelegramSender(HttpClient httpClient,
                          @Value("${telegram.bot-token}") String botToken,
                          @Value("${telegram.chat-id}") String chatId) {
        this.httpClient = httpClient;
        this.botToken = botToken;
        this.chatId = chatId;
    }

    public void sendPhoto(byte[] photo, String fileName) throws IOException, InterruptedException {
        String url = "https://api.telegram.org/bot" + botToken + "/sendPhoto";
        String boundary = "JavaHttpClientBoundary" + System.currentTimeMillis();

        byte[] multipartBody = buildMultipartBody(boundary, photo, fileName);

        HttpRequest request = HttpRequest.newBuilder()
                                            .uri(URI.create(url))
                                            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                                            .POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody))
                                            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Ошибка Telegram API: " + response.statusCode() + " " + response.body());
        }
    }

    private byte[] buildMultipartBody(String boundary, byte[] photo, String fileName) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"chat_id\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.write((chatId + "\r\n").getBytes(StandardCharsets.UTF_8));

        out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"photo\"; filename=\"" + fileName + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        out.write("Content-Type: application/octet-stream\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        out.write(photo);
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));

        out.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        return out.toByteArray();
    }

}
