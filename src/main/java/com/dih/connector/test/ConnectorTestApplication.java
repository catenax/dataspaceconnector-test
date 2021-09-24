package com.dih.connector.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ConnectorTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConnectorTestApplication.class, args);
    }
}
