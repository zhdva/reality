package org.zhadaev.reality;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Service
@EnableScheduling
public class Scheduler {

    @Value("${app.host}")
    private String host;

    @Value("${server.port}")
    private String port;

    private EmailService emailService;

    private boolean schedulerEnabled;

    private RestTemplate restTemplate;

    public Scheduler(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostConstruct
    private void init() {
        CloseableHttpClient httpClient = HttpClients.custom()
                                                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                                                    .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        restTemplate = new RestTemplate(requestFactory);
    }

    @Scheduled(fixedDelay = 600_000)
    public void restartSchedule() {
        schedulerEnabled = false;
        restTemplate.getForObject(host + "/startSchedule", Void.class);
    }

    public void startSchedule() {
        schedulerEnabled = true;
    }

    @Scheduled(fixedDelay = 3000)
    private void checkEmail() {
        if (schedulerEnabled) {
            emailService.checkEmail();
        }
    }

}
