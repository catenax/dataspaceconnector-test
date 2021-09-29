package com.dih.connector.test.service;

import com.dih.connector.test.client.connector.model.AgreementResponse;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerOfferService {

    @Value("${consumer.baseUrl}")
    private URI consumerBaseUrl;

    @Value("${producer.baseUrl}")
    private URI producerBaseUrl;

    @Qualifier("json-ld")
    private final RestTemplate restTemplateLd;

    @Qualifier("json-default")
    private final RestTemplate restTemplateDefault;

    @Qualifier("utf16string")
    private final RestTemplate restTemplateUtf16BEString;


    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        objectMapper.setDefaultPrettyPrinter(prettyPrinter);
    }

    public void consumeOffer(UUID offerId) throws IOException {
        var url = consumerBaseUrl + "/api/ids/description";
        HttpHeaders headers = new HttpHeaders();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("recipient", producerBaseUrl.normalize() + "/api/ids/data")
                .queryParam("elementId", producerBaseUrl.normalize() + "/api/offers/" + offerId.toString());
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        var body = restTemplateLd.exchange(
                builder.toUriString(),
                HttpMethod.POST,
                entity,
                JsonNode.class
        ).getBody();
        var permissionJsonNode = body.get("https://w3id.org/idsa/core/contractOffer").get("https://w3id.org/idsa/core/permission");
        var artifactNode = body.get("https://w3id.org/idsa/core/representation").get("https://w3id.org/idsa/core/instance");
        var agreementResponse = negotiateContract(permissionJsonNode, artifactNode, offerId);
        var movedData = getConsumerData(agreementResponse);
        log.info("Data: {}", movedData );
    }


    private AgreementResponse negotiateContract(JsonNode permissionJson, JsonNode artifactNode, UUID offerId) {
        var url = consumerBaseUrl + "/api/ids/contract";
        HttpHeaders headers = new HttpHeaders();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("recipient", producerBaseUrl.normalize() + "/api/ids/data")
                .queryParam("resourceIds", producerBaseUrl.normalize() + "/api/offers/" + offerId.toString())
                .queryParam("artifactIds", artifactNode.path("@id").asText())
                .queryParam("download", "true");

        var body = getContractAgreementPayload(permissionJson.get("@id").asText(), artifactNode.path("@id").asText());
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<JsonNode> entity = new HttpEntity<>(body, headers);
        return  restTemplateDefault.exchange(
                builder.toUriString(),
                HttpMethod.POST,
                entity,
                AgreementResponse.class
        ).getBody();
    }

    private JsonNode getContractAgreementPayload(String ruleId, String artifactId) {
        var node = objectMapper.createObjectNode()
                        .put("@type", "ids:Permission")
                        .put("@id", ruleId);
        node.putArray("ids:description")
                .addObject().put("@value", "provide-access").put("@type", "http://www.w3.org/2001/XMLSchema#string");
        node.putArray("ids:title")
                .addObject().put("@value", "Allow Data Usage").put("@type", "http://www.w3.org/2001/XMLSchema#string");
        node.putArray("ids:action")
                .addObject().put("@id", "https://w3id.org/idsa/code/USE");
        node.put("ids:target", artifactId);
        return objectMapper.createArrayNode().add(node);
    }

    private String getConsumerData(AgreementResponse agreementResponse) {
        var artifactsJson = restTemplateDefault.getForObject(agreementResponse.getSelfHref() + "/artifacts", JsonNode.class);
        var dataUrl = artifactsJson.get("_embedded").get("artifacts").get(0).get("_links").get("self").get("href").asText() + "/data";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(dataUrl)
                .queryParam("agreementUri", agreementResponse.getRemoteId())
                .queryParam("download", true);
        return restTemplateUtf16BEString.getForObject(builder.toUriString(), String.class);
    }
}
