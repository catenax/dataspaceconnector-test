package com.dih.connector.test.client.connector.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum DataSourceType {
    LOCAL("local"),
    HTTP_GET("http-get"),
    HTTPS_GET("https-get"),
    HTTPS_GET_BASICAUTH("https-get-basicauth");

    @Getter
    private String stringRepresentation;
}
