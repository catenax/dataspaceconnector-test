package com.dih.connector.test.client.connector.api;

import com.dih.connector.test.client.connector.DataspaceConnectorConfiguration;
import com.dih.connector.test.client.connector.model.GetListResponse;
import com.dih.connector.test.client.connector.model.OfferDescription;
import com.dih.connector.test.client.connector.model.OfferResponse;
import com.dih.connector.test.client.connector.model.RepresentationsList;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "DataspaceConnectorOffersApi", url = "placeholder", configuration = DataspaceConnectorConfiguration.class)
public interface DataspaceConnectorOffersApi {
    @PostMapping(path = "/offers", consumes = MediaType.APPLICATION_JSON_VALUE)
    OfferResponse registerOffer(URI baseUrl, @RequestBody OfferDescription metadata);

    @GetMapping(path = "/offers/{offerId}")
    OfferResponse getOffer(URI baseUrl, @PathVariable("offerId") UUID offerId);

    @PutMapping(path = "/offers/{offerId}")
    void updateOffer(URI baseUrl, @PathVariable("offerId") UUID offerId, @RequestBody OfferDescription metadata);

    @DeleteMapping(path = "/offers/{offerId}")
    void deleteOffer(URI baseUrl, @PathVariable("offerId") UUID offerId);

    @PostMapping(path = "/offers/{offerId}/representations")
    void linkRepresentations(URI baseUrl, @PathVariable("offerId") UUID offerId,
                             @RequestBody List<String> representations);

    @GetMapping(path = "/offers/{offerId}/representations")
    GetListResponse<RepresentationsList> getAllRepresentations(URI baseUrl, @PathVariable("offerId") UUID offerId);
}
