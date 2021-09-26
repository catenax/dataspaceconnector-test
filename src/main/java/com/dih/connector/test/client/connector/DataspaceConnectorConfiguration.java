package com.dih.connector.test.client.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jsonldjava.core.DocumentLoader;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;

@Configuration
public class DataspaceConnectorConfiguration {
    @Value("${connector.username}")
    private String username;

    @Value("${connector.password}")
    private String password;

    @Bean
    public BasicAuthRequestInterceptor adminAuth() {
        return new BasicAuthRequestInterceptor(username, password);
    }

    @Bean
    public BasicAuthenticationInterceptor authInterceptor() {
        return new BasicAuthenticationInterceptor(username, password);
    }

    @Bean(name = "json-ld")
    public RestTemplate restTemplate() {
        var restTemplate = new RestTemplate();
        var objectMapper = new ObjectMapper();
        restTemplate.getInterceptors().add(authInterceptor());
        var converter = new AbstractHttpMessageConverter<JsonNode>() {
            @Override
            protected boolean supports(Class<?> aClass) {
                return JsonNode.class.isAssignableFrom(aClass);
            }
            @Override
            protected JsonNode readInternal(Class<? extends JsonNode> aClass, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
                var jsonLd = JsonUtils.fromInputStream(httpInputMessage.getBody());
                var compacted =  JsonLdProcessor.compact(jsonLd, new HashMap<>(), new JsonLdOptions());
                return objectMapper.readTree(JsonUtils.toString(compacted));
            }
            @Override
            protected void writeInternal(JsonNode jsonNode, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
                try (var outputStream = new OutputStreamWriter(httpOutputMessage.getBody())) {
                    objectMapper.writeValue(outputStream, jsonNode);
                }
            }
        };
        converter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON));
        restTemplate.setMessageConverters(List.of(converter));
        return restTemplate;
    }

    @Bean(name = "json-default")
    public RestTemplate restTemplateDefault() {
        var rt = new RestTemplate();
        rt.getInterceptors().add(authInterceptor());
        return rt;
    }
}
