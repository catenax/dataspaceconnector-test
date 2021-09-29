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

    @Value("${offerId}")
    private UUID offerId;

    private final ProducerOfferService producerOfferService;
    private final ConsumerOfferService consumerOfferService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (TestModeEnum.REGISTER.equals(testMode)) {
            producerOfferService.createOffer();
        } else if (TestModeEnum.CONSUME.equals(testMode)) {
            consumerOfferService.consumeOffer(offerId);
        } else {
            var oferId = producerOfferService.createOffer();
            consumerOfferService.consumeOffer(oferId);
        }
    }
}
