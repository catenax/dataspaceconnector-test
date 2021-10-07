# Connector Test Application HOWTO
The source code of the test application is available here:  <https://github.com/catenax/dataspaceconnector-test>

## Test Application Configuration
In order to run the application it needed to be configured. Under **resources** folder the file **application.yml**
can be found. Here is the content of that file:
```yaml
logging:
    level.com.dih.connector.test: DEBUG
producer:
    username: admin
    password: password
    baseUrl: https://catenaxdev001akssrv.germanywestcentral.cloudapp.azure.com/producer
    data:
#        text: "Hello, world!"
        url: https://play.nintendo.com/images/Masthead_Kirby.17345b1513ac044897cfc243542899dce541e8dc.9afde10b.png
consumer:
    username: admin
    password: password
    baseUrl: https://catenaxdev001akssrv.germanywestcentral.cloudapp.azure.com/consumer
    data:
        text: false
        offerId: ff9eec5a-658f-404f-8dd7-53fe19adc0ab
```
Here we configure the Producer and the Consumer parts. Both have sections `username`, `password` and `baseUrl` where we 
shall specify corresponding information for the connectors.

Section `data` is responsible for the data we share. If `text` is set then the data published as a text in the Producer 
connector. If `url` is set then the data is going to be fetched from given url by request from the Consumer.

Corresponding section for the Consumer is responsible for specifying the format of data in `text` field (**true** if it 
is the text, **false** elsewhere) and `offerId` is responsible for the offer identifier if we want to run the
application in consumer-only mode. Then Producer part is skipped and the Consumer will try to get the data by provided
Id. If it is not set then the Producer and Consumer are started in a pipeline mode: first the Producer publishes 
the data, then the Consumer consumes it.

## Provider part
1. As a first step, we need to register a new offer. To do that, we're using `POST /api/offers` endpoint. As a request 
body we're passing filled OfferDescription DTO. Finally, we're saving offerId and self link to the offer:
```java
var offerResponse = offersApi.registerOffer(producerApiUri, getOfferDescription(testTimeMillis));
var offerId = offerResponse.getUUIDFromLink();
var offerSelfHref = offerResponse.getSelfHref();
```
2. Then, we're getting catalog (to not create new catalog for each request) using `GET /api/catalogs`  or creating new
catalog (if it does not exist) using `POST /api/catalogs` with filled CatalogDescription DTO as a request body and 
saving catalogId:
```java
var catalogResponse = Optional.ofNullable(catalogsApi.getAllCatalogs(producerApiUri))
        .map(GetListResponse::getEmbedded)
        .map(CatalogList::getCatalogs)
        .orElseGet(ArrayList::new)
        .stream()
        .filter(it -> TEST_CATALOG.equals(it.getTitle()))
        .findFirst()
        .orElseGet(() -> catalogsApi.createCatalog(producerApiUri, getCatalogDescription()));
var catalogId = catalogResponse.getUUIDFromLink();
```
3. Now we should create a link between offer and catalog using `POST /catalogs/{catalogId}/offers`

catalogId is taken from the previous step.
```java
catalogsApi.linkOffer(producerApiUri, catalogId, List.of(offerResponse.getSelfHref()));
```
4. As a next step, we need to create a rule, that will restrict access to the offer. For now we will use 
**PROVIDE_ACCESS** policy, that will allow everyone to access this offer. We will use `POST /api/rules`
with filled RuleDescription DTO. As a result we're saving ruleId and self link to rule for later usage:
```java
var ruleResponse = rulesApi.registerRule(producerApiUri, getRuleDescription(testTimeMillis));
var ruleId = ruleResponse.getUUIDFromLink();
var ruleSelfHref = ruleResponse.getSelfHref();
```
5. Now we need to create a contract. We will use *POST /api/contracts* with filled ContractDescription. As a 
contact end date we will use some far away date (2999-01-01). As a result we will save contractId:
```java
var contractResponse = contractsApi.createContract(producerApiUri, getContractDescription(testTimeMillis));
var contractId = contractResponse.getUUIDFromLink();
```
6. Then, we need to make a links: between contract and rule (`POST /api/contracts/{contractId}/rules`), and contract 
and offer (`POST /api/contracts/{contractId}/offers`):
```java
contractsApi.linkRules(producerApiUri, contractId, List.of(ruleSelfHref));
contractsApi.linkOffers(producerApiUri, contractId, List.of(offerSelfHref));
```
7. As a next step, we will create an offer representation - `POST /api/representations` with filled 
ResourceRepresentationDescription DTO. As a result we will save representationId and self link to representation:
```java
var representationResponse = representationsApi.registerRepresentation(producerApiUri, getRepresentation(testTimeMillis));
var representationId = representationResponse.getUUIDFromLink();
var representationSelfHref = representationResponse.getSelfHref();
```
8. Now we need to create an artifact - `POST /api/artifacts`  with filled ArtifactDescription DTO. Artifact - it is a 
final data, that will be requested from connector. We will save artifactId and self link to artifact:
```java
var artifactDescription = getArtifactDescription(testTimeMillis);
var artifactResponse = artifactsApi.registerArtifact(producerApiUri, artifactDescription);
var artifactId = artifactResponse.getUUIDFromLink();
var artifactSelfHref = artifactResponse.getSelfHref();
```
Let's take a closer look at *getArtifactDescription()*
```java
    private ArtifactDescription getArtifactDescription(long testTimeMillis) {
        var builder = ArtifactDescription.builder().title("Artifact_" + testTimeMillis);
        if (StringUtils.isNotBlank(dataText)) {
            builder.value(dataText);
        } else if (remoteDataUri != null) {
            builder.accessUrl(remoteDataUri);
        }
        return builder.build();
    }
```
Here is an important thing about the data we are about to publish: if it is a local text data then we put it into 
*value* field of the ArtifactDescription. The text will be stored at Connector side and returned by request in 
UTF-16 format.

If it is a remote data accessible from the Connector via HTTP then we put the link into *accessUri* field. Then the 
Connector retransmit the data available by this URL to the consumer Connector by its request.

9. As a last step we need to link artifact with its representation 
(`POST /api/representations/{representationId}/artifacts`) and link representation with the offer 
(`POST /api/offers/{offerId}/representations`).
```java
representationsApi.linkArtifacts(producerApiUri, representationId, List.of(artifactSelfHref));
offersApi.linkRepresentations(producerApiUri, offerId, List.of(representationSelfHref));
```

That's all. All saved information will be printed into console.
```java
log.info("Created: \nOffer {}\nCatalog {}\nRule {}\nContract {}\nRepresentation {}\nArtifact {}", offerId, catalogId,
        ruleId, contractId, representationId, artifactId);
log.info("Artifact description {}", artifactDescription);
```

## Consumer part

1. Knowing registered at producer side Offer ID we try to get more information about that offer. To do so we 
perform *POST* query. Take a note that we are querying the endpoint of the consumer to get the information about 
an offer registered at producer side:
```java
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
```
MessageConverter for the RestTemplate is configured so that it returns JsonNode in 'Compacted' format where the keys 
in JSON are represented in such a way where the identifiers are prefixed with their namespaces as in a URL.

2. From the description we can get JSON-LD which contains Permission node and Artifact node:
```java
    var permissionJsonNode = body.map(b -> b.get("https://w3id.org/idsa/core/contractOffer")).map(b -> b.get("https://w3id.org/idsa/core/permission"))
            .orElseThrow(() -> new RuntimeException("Cannot find Permission section in Offer Description"));
    var artifactNode = body.map(b->b.get("https://w3id.org/idsa/core/representation")).map(b -> b.get("https://w3id.org/idsa/core/instance"))
            .orElseThrow(() -> new RuntimeException("Cannot find Instance section in Offer Description"));
```
Artifact node and Permission node contains their identifiers in ‘@id’ field, where they can be taken from by 
`artifactNode.path("@id").asText()` and `permissionJsonNode.path("@id").asText()`

3. Performing negotiation of the contract agreement.
```java
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
```
Here we POST a json to the `consumerBaseUrl + "/api/ids/contract` endpoint of the consumer. The json has this format:
```json
[
  {
    "@type" : "ids:Permission",
    "@id" : "https://catenaxdev001akssrv.germanywestcentral.cloudapp.azure.com/producer/api/rules/e3a83b68-08d8-4933-8a4b-cf2bccff9caf",
    "ids:description" : [
      {
        "@value" : "provide-access",
        "@type" : "http://www.w3.org/2001/XMLSchema#string"
      }
    ],
    "ids:title" : [
      {
        "@value" : "Allow Data Usage",
        "@type" : "http://www.w3.org/2001/XMLSchema#string"
      }
    ],
    "ids:action" : [
      {
        "@id" : "https://w3id.org/idsa/code/USE"
      }
    ],
    "ids:target" : "https://catenaxdev001akssrv.germanywestcentral.cloudapp.azure.com/producer/api/artifacts/66fd228c-3106-45dd-ab35-ef61b54507f0"
  }
]
```
Where "@id" is rule identifier created when at producer stage. This identifier was taken from permissionJsonNode 
variable.  “ids:target” is an Artifact identifier taken from artifactNode variable which we obtained in step 2. 
Take a note that we shall use a 'shortified' namespaces, e.g. "ids:" instead of "https://w3id.org/idsa/core" 
while "https://w3id.org/idsa/code/USE" instead of "idsc:USE". The connector returns an error message if we ignore this.

4. `AgreementResponse` contains a reference to the agreement on consumer side. From this reference we take artifacts 
associated with this agreement:
```java
    var artifactsJson = Optional.ofNullable(
            restTemplateDefault.getForObject(agreementResponse.getSelfHref() + "/artifacts", JsonNode.class)
    );
```
Having artifactsJson we can construct an URL to retrieve the actual data registered at producer side:
```java
    var dataUrl = artifactsJson.map(aj -> aj.get("_embedded"))
            .map(em -> em.get("artifacts"))
            .map(a -> a.get(0))
            .map(z -> z.get("_links"))
            .map(l -> l.get("self"))
            .map(s -> s.get("href"))
            .map(JsonNode::asText)
            .map(s -> s.concat("/data"))
            .orElseThrow( () -> new RuntimeException("Couldn't construct data retrieval URL from Artifact JSON"));
```
Take a note, the dataUrl is pointing to the consumer connector.

Now, having this URL, we can retrieve actual data. If it was registered by the producer as a text then we get it 
in UTF-16 format.
```java
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(dataUrl)
            .queryParam("agreementUri", agreementResponse.getRemoteId())
            .queryParam("download", true);
    return restTemplateUtf16BEString.getForObject(builder.toUriString(), String.class);
```
If it was registered as an external URL then the Consumer connector retransmits original octet stream to the client 
without any conversion.