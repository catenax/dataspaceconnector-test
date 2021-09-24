package com.dih.connector.test.client.connector;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URI;

@FeignClient(name = "DataspaceConnectorConnectorApi", url = "placeholder", configuration = DataspaceConnectorConfiguration.class)
public interface DataspaceConnectorConnectorApi {
    @GetMapping(path = "/connector")
    String getConnectorDescription(URI baseUrl);
}
