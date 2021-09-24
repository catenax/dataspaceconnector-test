package com.dih.connector.test.service;

import com.dih.connector.test.dto.TestModeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandLineRunnerService implements ApplicationRunner {
    @Value("${test.mode}")
    private TestModeEnum testMode;

    private final CreateOfferService createOfferService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (TestModeEnum.REGISTER.equals(testMode)) {
            createOfferService.createOffer();
        } else {
            throw new RuntimeException("Only REGISTER mode is implemented");
        }
    }
}
