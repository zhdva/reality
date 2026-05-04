package org.zhadaev.reality;

import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

import java.net.http.HttpClient;
import java.time.Duration;

@Factory
public class HttpClientFactory {

    @Singleton
    public HttpClient javaNetHttpClient() {
        return HttpClient.newBuilder()
                         .connectTimeout(Duration.ofSeconds(10))
                         .followRedirects(HttpClient.Redirect.NORMAL)
                         .build();
    }

}
