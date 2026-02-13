package org.zhadaev.reality;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Service
@EnableScheduling
public class Scheduler {

    private final EmailService emailService;

    @Value("${app.host}")
    private String host;

    private final RestTemplate restTemplate = new RestTemplate();;

    public Scheduler(EmailService emailService) {
        this.emailService = emailService;
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    private void crutch() {
        restTemplate.getForObject(host + "/reality/crutch", Void.class);
    }

    @Scheduled(fixedDelay = (10000/TelegramBot.MESSAGES_PER_SECOND_LIMIT))
    public void checkEmail() {
        emailService.checkEmail();
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.DAYS)
    public void removeUnnecessary() {
        emailService.removeUnnecessary();
    }

}
