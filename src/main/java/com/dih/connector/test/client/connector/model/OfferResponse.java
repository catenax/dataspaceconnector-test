package com.dih.connector.test.client.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OfferResponse extends LinkedDTO {
    private String title;
    private String description;
    private Long version;
    private List<String> keywords;
    private URI publisher;
    private String language = "EN";
    private URI sovereign;
    private URI licence;
    private URI endpointDocumentation;

    @Override
    public String getApiName() {
        return "offers/";
    }
}
