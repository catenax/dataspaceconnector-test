package com.dih.connector.test.client.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceRepresentationDescription {
    private String title;
    private String description;
    private String mediaType;
    private String language;
    private String standard;
}
