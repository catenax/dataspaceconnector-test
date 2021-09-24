package com.dih.connector.test.client.connector;

import com.dih.connector.test.client.connector.model.CatalogDescription;
import com.dih.connector.test.client.connector.model.CatalogList;
import com.dih.connector.test.client.connector.model.CatalogResponse;
import com.dih.connector.test.client.connector.model.GetListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "DataspaceConnectorCatalogsApi", url = "placeholder", configuration = DataspaceConnectorConfiguration.class)
public interface DataspaceConnectorCatalogsApi {
    @PostMapping(path = "/catalogs", consumes = MediaType.APPLICATION_JSON_VALUE)
    CatalogResponse createCatalog(URI baseUrl, @RequestBody CatalogDescription catalogDescription);

    @PostMapping(path = "/catalogs/{catalogId}/offers")
    void linkOffer(URI baseUrl, @PathVariable("catalogId") UUID catalogId, @RequestBody List<String> offers);

    @GetMapping(path = "/catalogs")
    GetListResponse<CatalogList> getAllCatalogs(URI baseUrl);
}
