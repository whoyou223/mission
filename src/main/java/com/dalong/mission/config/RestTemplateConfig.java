package com.dalong.mission.config;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

/**
 * Package: com.lianjia.confucius.mooc.config
 *
 * @author: 175405@lianjia.com
 * Date: 2017/12/25
 */
@Configuration
public class RestTemplateConfig {


    @Bean(name = "restTemplate")
    public RestTemplate restTemplate() {
        HttpClient httpClient = HttpClientBuilder
                .create()
                .disableCookieManagement()
                .disableAutomaticRetries()
                .disableRedirectHandling()
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setConnectTimeout(5 * 1000)
                                .setSocketTimeout(2 * 1000)
                                .setConnectionRequestTimeout(200)
                                .build())
                .setMaxConnTotal(1000)
                .setMaxConnPerRoute(500)
                .setConnectionTimeToLive(1L, TimeUnit.MINUTES)
                .build();
        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(requestFactory);
    }
}
