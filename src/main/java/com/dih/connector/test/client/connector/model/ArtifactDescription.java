package com.dih.connector.test.client.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtifactDescription {
    private URI remoteAddress;
    private String title;
    private URI accessUrl;
    private String username;
    private String password;
    private String value;
    private boolean automatedDownload = true;
}
