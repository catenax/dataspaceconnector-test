package com.dih.connector.test.client.connector;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

@FeignClient(name = "DataspaceConnectorBrokerApi", url = "placeholder", configuration = DataspaceConnectorConfiguration.class)
public interface DataspaceConnectorBrokerApi {
    @PostMapping(path = "/ids/connector/update")
    void registerOrUpdateConnector(URI baseUrl, @RequestParam("recipient") String brokerUrl);

    @PostMapping(path = "/ids/connector/unavailable")
    void unregisterConnector(URI baseUrl, @RequestParam("recipient") String brokerUrl);

    @PostMapping(path = "/ids/resource/update")
    void updateResource(URI baseUrl, @RequestParam("resourceId") String resourceId, @RequestParam("recipient") String brokerUrl);

    @PostMapping(path = "/ids/resource/unavailable")
    void removeResource(URI baseUrl, @RequestParam("resourceId") String resourceId, @RequestParam("recipient") String brokerUrl);

}
