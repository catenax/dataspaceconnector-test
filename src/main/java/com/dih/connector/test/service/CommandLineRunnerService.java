package com.dih.connector.test.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandLineRunnerService implements ApplicationRunner {
    @Value("${consumer.data.offerId:#{null}}")
    private UUID offerId;

    private final ProducerOfferService producerOfferService;
    private final ConsumerOfferService consumerOfferService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (Objects.isNull(offerId)) {
            offerId = producerOfferService.createOffer();
        }
        consumerOfferService.consumeOffer(offerId);
    }
}
