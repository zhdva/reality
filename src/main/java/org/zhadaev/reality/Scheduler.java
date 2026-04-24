package org.zhadaev.reality;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;

@Singleton
public class Scheduler {

    private final EmailService emailService;

    private final HttpClient httpClient;

    public Scheduler(@Client("${app.host}") HttpClient httpClient, EmailService emailService) {
        this.httpClient = httpClient;
        this.emailService = emailService;
    }

    @Scheduled(fixedDelay = "10s")
    void crutch() {
        httpClient.toBlocking().exchange(
                HttpRequest.GET("/reality/crutch")
        );
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
