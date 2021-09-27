package com.dih.connector.test.client.connector.api;

import com.dih.connector.test.client.connector.DataspaceConnectorConfiguration;
import com.dih.connector.test.client.connector.model.RuleDescription;
import com.dih.connector.test.client.connector.model.RuleResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;

@FeignClient(name = "DataspaceConnectorRulesApi", url = "placeholder", configuration = DataspaceConnectorConfiguration.class)
public interface DataspaceConnectorRulesApi {
    @PostMapping(path = "/rules", consumes = MediaType.APPLICATION_JSON_VALUE)
    RuleResponse registerRule(URI baseUrl, @RequestBody RuleDescription metadata);
}
