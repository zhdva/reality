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
import java.util.concurrent.TimeUnit;

@Service
@EnableScheduling
public class Scheduler {

    @Value("${app.host}")
    private String host;

    @Value("${server.port}")
    private String port;

    private RestTemplate restTemplate;

    @PostConstruct
    private void init() {
        CloseableHttpClient httpClient = HttpClients.custom()
                                                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                                                    .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        restTemplate = new RestTemplate(requestFactory);
    }

    @Scheduled(fixedDelay = (1000/TelegramBot.MESSAGES_PER_SECOND_LIMIT))
    private void checkEmail() {
        restTemplate.postForObject(host + "/email/process", null, Void.class);
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.DAYS)
    private void removeUnnecessary() {
        restTemplate.delete(host + "/email/unnecessary");
    }

}
