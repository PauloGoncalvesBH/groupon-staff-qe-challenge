package com.groupon.orders;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OrdersHttpConfig {

    @Bean
    public RestTemplate restTemplate(
            @Value("${HTTP_CONNECT_TIMEOUT_MS:3000}") int connectTimeoutMs,
            @Value("${HTTP_READ_TIMEOUT_MS:3000}") int readTimeoutMs) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return new RestTemplate(factory);
    }
}
