package com.dih.connector.test.service;

import com.dih.connector.test.TestModeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandLineRunnerService implements ApplicationRunner {
    @Value("${test.mode}")
    private TestModeEnum testMode;

    private final CreateOfferService createOfferService;
    private final ConsumeOfferService consumeOfferService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
//        if (TestModeEnum.REGISTER.equals(testMode)) {
//            createOfferService.createOffer();
//        } else {
//            consumeOfferService.consumeOffer(UUID.fromString("9bda348d-ff44-4f1b-9270-a9c60b976f73"));
//        }
        var oferId = createOfferService.createOffer();
        consumeOfferService.consumeOffer(oferId);
        //consumeOfferService.consumeOffer(UUID.fromString("73b72808-b29a-454c-9820-83fc0d85abbc"));
    }
}
