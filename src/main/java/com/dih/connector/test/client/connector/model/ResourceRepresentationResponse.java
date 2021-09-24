package com.dih.connector.test.client.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceRepresentationResponse extends LinkedDTO {
    private String title;
    private String mediaType;
    private String language;

    @Override
    public String getApiName() {
        return "representations/";
    }
}
