package com.dih.connector.test.client.connector;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class DataspaceConnectorConfiguration {
    @Value("${connector.username}")
    private String username;

    @Value("${connector.password}")
    private String password;

    @Bean
    public BasicAuthRequestInterceptor adminAuth() {
        return new BasicAuthRequestInterceptor(username, password);
    }
}
