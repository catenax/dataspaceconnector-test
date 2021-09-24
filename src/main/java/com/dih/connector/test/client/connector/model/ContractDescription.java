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
public class ContractDescription {
    private URI consumer;
    private URI provider;
    private String title;
    // since there is a bug in DataspaceConnector - start and end using type String instead of ZonedDateTime
    private String start;
    private String end;
}
