package com.dih.connector.test.client.connector.api;

import com.dih.connector.test.client.connector.DataspaceConnectorConfiguration;
import com.dih.connector.test.client.connector.model.ArtifactsList;
import com.dih.connector.test.client.connector.model.GetListResponse;
import com.dih.connector.test.client.connector.model.ResourceRepresentationDescription;
import com.dih.connector.test.client.connector.model.ResourceRepresentationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "DataspaceConnectorRepresentationsApi", url = "placeholder", configuration = DataspaceConnectorConfiguration.class)
public interface DataspaceConnectorRepresentationsApi {
    @PostMapping(path = "/representations", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResourceRepresentationResponse registerRepresentation(URI baseUrl, @RequestBody ResourceRepresentationDescription metadata);

    @GetMapping(path = "/representations/{representationId}")
    ResourceRepresentationResponse getRepresentation(URI baseUrl, @PathVariable("representationId") UUID representationId);

    @PutMapping(path = "/representations/{representationId}")
    void updateRepresentation(URI baseUrl, @PathVariable("representationId") UUID representationId, @RequestBody ResourceRepresentationDescription metadata);

    @DeleteMapping(path = "/representations/{representationId}")
    void deleteRepresentation(URI baseUrl, @PathVariable("representationId") UUID representationId);

    @PostMapping(path = "/representations/{representationId}/artifacts", consumes = MediaType.APPLICATION_JSON_VALUE)
    void linkArtifacts(URI baseUrl, @PathVariable("representationId") UUID representationId,
                       @RequestBody List<String> artifacts);

    @GetMapping(path = "/representations/{representationId}/artifacts")
    GetListResponse<ArtifactsList> getAllArtifacts(URI baseUrl, @PathVariable("representationId") UUID representationId);
}
