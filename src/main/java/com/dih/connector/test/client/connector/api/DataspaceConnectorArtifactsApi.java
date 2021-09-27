package com.dih.connector.test.client.connector.api;

import com.dih.connector.test.client.connector.DataspaceConnectorConfiguration;
import com.dih.connector.test.client.connector.model.ArtifactDescription;
import com.dih.connector.test.client.connector.model.ArtifactResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import java.util.UUID;

@FeignClient(name = "DataspaceConnectorArtifactsApi", url = "placeholder", configuration = DataspaceConnectorConfiguration.class)
public interface DataspaceConnectorArtifactsApi {
    @PostMapping(path = "/artifacts", consumes = MediaType.APPLICATION_JSON_VALUE)
    ArtifactResponse registerArtifact(URI baseUrl, @RequestBody ArtifactDescription metadata);

    @GetMapping(path = "/artifacts/{artifactId}")
    ArtifactResponse getArtifact(URI baseUrl, @PathVariable("artifactId") UUID artifactId);

    @PutMapping(path = "/artifacts/{artifactId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    void updateArtifact(URI baseUrl, @PathVariable("artifactId") UUID artifactId,
                        @RequestBody ArtifactDescription metadata);

    @DeleteMapping(path = "/artifacts/{artifactId}")
    void deleteArtifact(URI baseUrl, @PathVariable("artifactId") UUID artifactId);
}
