package com.dih.connector.test.client.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OfferDescription {
    private String title;
    private String description;
    private List<String> keywords;
    private URI publisher;
    private String language;
    private URI sovereign;
    private URI licence;
    private URI endpointDocumentation;
}
