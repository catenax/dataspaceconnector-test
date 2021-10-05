package com.dih.connector.test.service;

import com.dih.connector.test.client.connector.api.DataspaceConnectorArtifactsApi;
import com.dih.connector.test.client.connector.api.DataspaceConnectorCatalogsApi;
import com.dih.connector.test.client.connector.api.DataspaceConnectorContractsApi;
import com.dih.connector.test.client.connector.api.DataspaceConnectorOffersApi;
import com.dih.connector.test.client.connector.api.DataspaceConnectorRepresentationsApi;
import com.dih.connector.test.client.connector.api.DataspaceConnectorRulesApi;
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
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProducerOfferService {
    private static final String TEST_CATALOG = "test_catalog";
    private static final String PROVIDE_ACCESS_POLICY = "{ \"@context\" : { \"ids\" : \"https://w3id.org/idsa/core/\", \"idsc\" : \"https://w3id.org/idsa/code/\" }, \"@type\" : \"ids:Permission\", \"@id\" : \"https://w3id.org/idsa/autogen/permission/658ca300-4042-4804-839a-3c9548dcc26e\", \"ids:action\" : [ { \"@id\" : \"https://w3id.org/idsa/code/USE\" } ], \"ids:description\" : [ { \"@value\" : \"provide-access\", \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\" } ], \"ids:title\" : [ { \"@value\" : \"Allow Data Usage\", \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\" } ] }";
    private static final String CONTRACT_END_DATE = LocalDate.of(2999, 1, 1).atStartOfDay(ZoneOffset.UTC).toString();

    @Value("${producer.baseUrl}")
    private URI producerBaseUrl;

    @Value("${producer.data.text:#{null}}")
    private String dataText;

    @Value("${producer.data.url:#{null}}")
    private URI remoteDataUri;

    private final DataspaceConnectorOffersApi offersApi;
    private final DataspaceConnectorCatalogsApi catalogsApi;
    private final DataspaceConnectorRulesApi rulesApi;
    private final DataspaceConnectorContractsApi contractsApi;
    private final DataspaceConnectorRepresentationsApi representationsApi;
    private final DataspaceConnectorArtifactsApi artifactsApi;

    private URI producerApiUri;

    @PostConstruct
    public void init() {
        producerApiUri = producerBaseUrl.resolve(producerBaseUrl.getPath() + "/api");
    }

    public UUID createOffer() throws IOException {
        var testTimeMillis = System.currentTimeMillis();

        // create offer
        var offerResponse = offersApi.registerOffer(producerApiUri, getOfferDescription(testTimeMillis));
        var offerId = offerResponse.getUUIDFromLink();
        var offerSelfHref = offerResponse.getSelfHref();

        // get or create catalog
        var catalogResponse = Optional.ofNullable(catalogsApi.getAllCatalogs(producerApiUri))
                .map(GetListResponse::getEmbedded)
                .map(CatalogList::getCatalogs)
                .orElseGet(ArrayList::new)
                .stream()
                .filter(it -> TEST_CATALOG.equals(it.getTitle()))
                .findFirst()
                .orElseGet(() -> catalogsApi.createCatalog(producerApiUri, getCatalogDescription()));
        var catalogId = catalogResponse.getUUIDFromLink();

        // link offer with catalog
        catalogsApi.linkOffer(producerApiUri, catalogId, List.of(offerResponse.getSelfHref()));

        // create rule
        var ruleResponse = rulesApi.registerRule(producerApiUri, getRuleDescription(testTimeMillis));
        var ruleId = ruleResponse.getUUIDFromLink();
        var ruleSelfHref = ruleResponse.getSelfHref();

        // create contract
        var contractResponse = contractsApi.createContract(producerApiUri, getContractDescription(testTimeMillis));
        var contractId = contractResponse.getUUIDFromLink();

        contractsApi.linkRules(producerApiUri, contractId, List.of(ruleSelfHref));
        contractsApi.linkOffers(producerApiUri, contractId, List.of(offerSelfHref));

        // create representation
        var representationResponse = representationsApi.registerRepresentation(producerApiUri, getRepresentation(testTimeMillis));
        var representationId = representationResponse.getUUIDFromLink();
        var representationSelfHref = representationResponse.getSelfHref();

        // create artifact
        var artifactDescription = getArtifactDescription(testTimeMillis);
        var artifactResponse = artifactsApi.registerArtifact(producerApiUri, artifactDescription);
        var artifactId = artifactResponse.getUUIDFromLink();
        var artifactSelfHref = artifactResponse.getSelfHref();

        // link artifact with representation
        representationsApi.linkArtifacts(producerApiUri, representationId, List.of(artifactSelfHref));

        // link representation with resource
        offersApi.linkRepresentations(producerApiUri, offerId, List.of(representationSelfHref));

        log.info("Created: \nOffer {}\nCatalog {}\nRule {}\nContract {}\nRepresentation {}\nArtifact {}", offerId, catalogId,
                ruleId, contractId, representationId, artifactId);
        log.info("Artifact description {}", artifactDescription);
        if (Objects.nonNull(remoteDataUri)) {
            try (InputStream is = remoteDataUri.toURL().openConnection().getInputStream()) {
                String md5 = DigestUtils.md5Hex(is);
                log.info("Remote data MD5SUM={}", md5);
            }
        }
        return offerId;
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
        var builder = ArtifactDescription.builder().title("Artifact_" + testTimeMillis);
        if (StringUtils.isNotBlank(dataText)) {
            builder.value(dataText);
        } else if (remoteDataUri != null) {
            builder.accessUrl(remoteDataUri);
        }
        return builder.build();
    }
}
