package org.zhadaev.reality;

import io.micronaut.context.annotation.Value;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Singleton
public class Scheduler {

    private final EmailService emailService;

    private final HttpClient httpClient;

    private final String appHost;

    public Scheduler(HttpClient httpClient, EmailService emailService, @Value("${app.host}") String appHost) {
        this.httpClient = httpClient;
        this.emailService = emailService;
        this.appHost = appHost;
    }

    @Scheduled(fixedDelay = "10s")
    void crutch() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(URI.create(appHost + "/reality/crutch"))
                                         .header("Content-Type", "application/json")
                                         .GET()
                                         .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            System.out.println("Костыль не вставлен: [" + response.statusCode() + "] " + response.body());
        }
    }

    @Scheduled(fixedDelay = "1s")
    void checkMail() {
        emailService.checkEmail();
    }

    @Scheduled(fixedDelay = "1d")
    void removeUnnecessary() {
        emailService.removeUnnecessary();
    }

}
