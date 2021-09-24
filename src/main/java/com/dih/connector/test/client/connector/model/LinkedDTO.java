package com.dih.connector.test.client.connector.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class LinkedDTO {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private ZonedDateTime creationDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private ZonedDateTime modificationDate;
    @JsonProperty("_links")
    private ResourceLinks links;

    public abstract String getApiName();

    public UUID getUUIDFromLink() {
        return links.getUUIDFromLink(getApiName());
    }

    public String getSelfHref() {
        return links.getSelf().getHref();
    }
}
