package com.dih.connector.test.service;

import com.dih.connector.test.client.connector.DataspaceConnectorArtifactsApi;
import com.dih.connector.test.client.connector.DataspaceConnectorCatalogsApi;
import com.dih.connector.test.client.connector.DataspaceConnectorContractsApi;
import com.dih.connector.test.client.connector.DataspaceConnectorOffersApi;
import com.dih.connector.test.client.connector.DataspaceConnectorRepresentationsApi;
import com.dih.connector.test.client.connector.DataspaceConnectorRulesApi;
import com.dih.connector.test.client.connector.model.ArtifactDescription;
import com.dih.connector.test.client.connector.model.CatalogDescription;
import com.dih.connector.test.client.connector.model.CatalogList;
import com.dih.connector.test.client.connector.model.ContractDescription;
import com.dih.connector.test.client.connector.model.GetListResponse;
import com.dih.connector.test.client.connector.model.OfferDescription;
import com.dih.connector.test.client.connector.model.ResourceRepresentationDescription;
import com.dih.connector.test.client.connector.model.RuleDescription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOfferService {
    private static final String TEST_CATALOG = "test_catalog";
    private static final String PROVIDE_ACCESS_POLICY = "{ \"@context\" : { \"ids\" : \"https://w3id.org/idsa/core/\", \"idsc\" : \"https://w3id.org/idsa/code/\" }, \"@type\" : \"ids:Permission\", \"@id\" : \"https://w3id.org/idsa/autogen/permission/658ca300-4042-4804-839a-3c9548dcc26e\", \"ids:action\" : [ { \"@id\" : \"https://w3id.org/idsa/code/USE\" } ], \"ids:description\" : [ { \"@value\" : \"provide-access\", \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\" } ], \"ids:title\" : [ { \"@value\" : \"Allow Data Usage\", \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\" } ] }";
    private static final String CONTRACT_END_DATE = LocalDate.of(2999, 1, 1).atStartOfDay(ZoneOffset.UTC).toString();

    @Value("${connector.baseUrl}")
    private URI connectorBaseUrl;

    private final DataspaceConnectorOffersApi offersApi;
    private final DataspaceConnectorCatalogsApi catalogsApi;
    private final DataspaceConnectorRulesApi rulesApi;
    private final DataspaceConnectorContractsApi contractsApi;
    private final DataspaceConnectorRepresentationsApi representationsApi;
    private final DataspaceConnectorArtifactsApi artifactsApi;

    public void createOffer() {
        var testTimeMillis = System.currentTimeMillis();

        // create offer
        var offerResponse = offersApi.registerOffer(connectorBaseUrl, getOfferDescription(testTimeMillis));
        var offerId = offerResponse.getUUIDFromLink();
        var offerSelfHref = offerResponse.getSelfHref();

        // get or create catalog
        var catalogResponse = Optional.ofNullable(catalogsApi.getAllCatalogs(connectorBaseUrl))
                .map(GetListResponse::getEmbedded)
                .map(CatalogList::getCatalogs)
                .orElseGet(ArrayList::new)
                .stream()
                .filter(it -> TEST_CATALOG.equals(it.getTitle()))
                .findFirst()
                .orElseGet(() -> catalogsApi.createCatalog(connectorBaseUrl, getCatalogDescription()));
        var catalogId = catalogResponse.getUUIDFromLink();

        // link offer with catalog
        catalogsApi.linkOffer(connectorBaseUrl, catalogId, List.of(offerResponse.getSelfHref()));

        // create rule
        var ruleResponse = rulesApi.registerRule(connectorBaseUrl, getRuleDescription(testTimeMillis));
        var ruleId = ruleResponse.getUUIDFromLink();
        var ruleSelfHref = ruleResponse.getSelfHref();

        // create contract
        var contractResponse = contractsApi.createContract(connectorBaseUrl, getContractDescription(testTimeMillis));
        var contractId = contractResponse.getUUIDFromLink();

        contractsApi.linkRules(connectorBaseUrl, contractId, List.of(ruleSelfHref));
        contractsApi.linkOffers(connectorBaseUrl, contractId, List.of(offerSelfHref));

        // create representation
        var representationResponse = representationsApi.registerRepresentation(connectorBaseUrl, getRepresentation(testTimeMillis));
        var representationId = representationResponse.getUUIDFromLink();
        var representationSelfHref = representationResponse.getSelfHref();

        // create artifact
        var artifactDescription = getArtifactDescription(testTimeMillis);
        var artifactResponse = artifactsApi.registerArtifact(connectorBaseUrl, artifactDescription);
        var artifactId = artifactResponse.getUUIDFromLink();
        var artifactSelfHref = artifactResponse.getSelfHref();

        // link artifact with representation
        representationsApi.linkArtifacts(connectorBaseUrl, representationId, List.of(artifactSelfHref));

        // link representation with resource
        offersApi.linkRepresentations(connectorBaseUrl, offerId, List.of(representationSelfHref));

        log.info("Created: \nOffer {}\nCatalog {}\nRule {}\nContract {}\nRepresentation {}\nArtifact {}", offerId, catalogId,
                ruleId, contractId, representationId, artifactId);
        log.info("Artifact description {}", artifactDescription);
    }

    private OfferDescription getOfferDescription(long testTimeMillis) {
        return OfferDescription.builder()
                .title("testOffer-" + testTimeMillis)
                .description("testOffer-" + testTimeMillis)
                .publisher(URI.create("http://localhost"))
                .language("EN")
                .sovereign(URI.create("http://localhost"))
                .build();
    }

    private CatalogDescription getCatalogDescription() {
        return CatalogDescription.builder()
                .title(TEST_CATALOG)
                .description(TEST_CATALOG)
                .build();
    }

    private RuleDescription getRuleDescription(long testTimeMillis) {
        return RuleDescription.builder()
                .title("Rule_" + testTimeMillis)
                .value(PROVIDE_ACCESS_POLICY)
                .build();
    }

    private ContractDescription getContractDescription(long testTimeMillis) {
        return ContractDescription.builder()
                .title("Contract_" + testTimeMillis)
                .start(ZonedDateTime.now(ZoneOffset.UTC).toString())
                .end(CONTRACT_END_DATE)
                .build();
    }

    private ResourceRepresentationDescription getRepresentation(long testTimeMillis) {
        return ResourceRepresentationDescription.builder()
                .title("Representation_" + testTimeMillis)
                .description("Representation_" + testTimeMillis)
                .mediaType(MediaType.APPLICATION_JSON_VALUE)
                .language("EN")
                .build();
    }

    private ArtifactDescription getArtifactDescription(long testTimeMillis) {
        return ArtifactDescription.builder()
                .title("Artifact_" + testTimeMillis)
                .value("Content: " + UUID.randomUUID())
                .build();
    }
}
