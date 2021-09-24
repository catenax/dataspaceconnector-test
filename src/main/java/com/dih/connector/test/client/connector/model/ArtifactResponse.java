package com.dih.connector.test.client.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.net.URI;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtifactResponse extends LinkedDTO {
    private URI remoteId;
    private String title;
    private Long numAccessed;
    private Long byteSize;
    private Long checkSum;

    @Override
    public String getApiName() {
        return "artifacts/";
    }
}
