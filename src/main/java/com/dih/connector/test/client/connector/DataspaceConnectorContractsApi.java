package com.dih.connector.test.client.connector;

import com.dih.connector.test.client.connector.model.ContractDescription;
import com.dih.connector.test.client.connector.model.ContractResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "DataspaceConnectorContractsApi", url = "placeholder", configuration = DataspaceConnectorConfiguration.class)
public interface DataspaceConnectorContractsApi {
    @PostMapping(path = "/contracts", consumes = MediaType.APPLICATION_JSON_VALUE)
    ContractResponse createContract(URI baseUrl, @RequestBody ContractDescription contractDescription);

    @PostMapping(path = "/contracts/{id}/rules", consumes = MediaType.APPLICATION_JSON_VALUE)
    void linkRules(URI baseUrl, @PathVariable("id") UUID id, @RequestBody List<String> rules);

    @PostMapping(path = "/contracts/{id}/offers", consumes = MediaType.APPLICATION_JSON_VALUE)
    void linkOffers(URI baseUrl, @PathVariable("id") UUID id, @RequestBody List<String> offers);
}
