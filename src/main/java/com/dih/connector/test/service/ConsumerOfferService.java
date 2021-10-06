package com.dih.connector.test.service;

import com.dih.connector.test.client.connector.model.AgreementResponse;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerOfferService {

    @Value("${consumer.baseUrl}")
    private URI consumerBaseUrl;

    @Value("${producer.baseUrl}")
    private URI producerBaseUrl;

    @Value("${consumer.data.text:false}")
    private boolean isText;

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
        var prettyPrinter = new DefaultPrettyPrinter();
        prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        objectMapper.setDefaultPrettyPrinter(prettyPrinter);
    }

    public void consumeOffer(UUID offerId) {
        var url = consumerBaseUrl + "/api/ids/description";
        var headers = new HttpHeaders();
        var builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("recipient", producerBaseUrl.normalize() + "/api/ids/data")
                .queryParam("elementId", producerBaseUrl.normalize() + "/api/offers/" + offerId.toString());
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        var entity = new HttpEntity<String>(headers);
        var body = Optional.ofNullable(
                restTemplateLd.postForObject(
                        builder.toUriString(),
                        entity,
                        JsonNode.class
                )
        );
        var permissionJsonNode = body.map(b -> b.get("https://w3id.org/idsa/core/contractOffer")).map(b -> b.get("https://w3id.org/idsa/core/permission"))
                .orElseThrow(() -> new RuntimeException("Cannot find Permission section in Offer Description"));
        var artifactNode = body.map(b->b.get("https://w3id.org/idsa/core/representation")).map(b -> b.get("https://w3id.org/idsa/core/instance"))
                .orElseThrow(() -> new RuntimeException("Cannot find Instance section in Offer Description"));
        var agreementResponse = negotiateContract(permissionJsonNode, artifactNode, offerId);
        if (isText) {
            var movedData = getConsumerData(agreementResponse, dataUrl -> restTemplateUtf16BEString.getForObject(dataUrl, String.class));
            log.info("Data: {}", movedData );
        } else {
            byte[] data = getConsumerData(agreementResponse, dataUrl -> restTemplateDefault.getForObject(dataUrl, byte[].class));
            String md5 = DigestUtils.md5Hex(data);
            log.info("Consumer data MD5SUM={}", md5 );
        }
    }


    private AgreementResponse negotiateContract(JsonNode permissionJson, JsonNode artifactNode, UUID offerId) {
        var url = consumerBaseUrl + "/api/ids/contract";
        var headers = new HttpHeaders();
        var builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("recipient", producerBaseUrl.normalize() + "/api/ids/data")
                .queryParam("resourceIds", producerBaseUrl.normalize() + "/api/offers/" + offerId.toString())
                .queryParam("artifactIds", artifactNode.path("@id").asText())
                .queryParam("download", "true");

        var body = getContractAgreementPayload(permissionJson.get("@id").asText(), artifactNode.path("@id").asText());
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        var entity = new HttpEntity<>(body, headers);
        return  restTemplateDefault.postForObject(
                builder.toUriString(),
                entity,
                AgreementResponse.class
        );
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

    private <T> T getConsumerData(AgreementResponse agreementResponse, Function<String, T> httpGet) {
        var artifactsJson = Optional.ofNullable(
                restTemplateDefault.getForObject(agreementResponse.getSelfHref() + "/artifacts", JsonNode.class)
        );
        var dataUrl = artifactsJson.map(aj -> aj.get("_embedded"))
                .map(em -> em.get("artifacts"))
                .map(a -> a.get(0))
                .map(z -> z.get("_links"))
                .map(l -> l.get("self"))
                .map(s -> s.get("href"))
                .map(JsonNode::asText)
                .map(s -> s.concat("/data"))
                .orElseThrow( () -> new RuntimeException("Couldn't construct data retrieval URL from Artifact JSON"));
        var builder = UriComponentsBuilder.fromHttpUrl(dataUrl)
                .queryParam("agreementUri", agreementResponse.getRemoteId())
                .queryParam("download", true);
        return httpGet.apply(builder.toUriString());
    }

}
