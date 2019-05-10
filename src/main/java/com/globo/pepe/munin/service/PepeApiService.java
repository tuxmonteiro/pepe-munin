package com.globo.pepe.munin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.globo.pepe.common.services.JsonLoggerService;
import com.globo.pepe.munin.util.MuninConfiguration;
import org.openstack4j.api.OSClient.OSClientV3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PepeApiService {

    RestTemplate restTemplate;

    private final JsonLoggerService jsonLoggerService;

    @Autowired
    private MuninConfiguration configuration;

    public PepeApiService(JsonLoggerService jsonLoggerService) {
        this.restTemplate = getRestTemplate();
        this.jsonLoggerService = jsonLoggerService;
    }

    public void sendMetrics(JsonNode metric, OSClientV3 osClientV3){
        HttpEntity<JsonNode> entity = new HttpEntity<>(metric);
        try {
            restTemplate.exchange(configuration.getPepeApiEndpoint(), HttpMethod.POST, entity, JsonNode.class);
        }
        catch (Exception e) {
            jsonLoggerService.newLogger(getClass()).put("short_message", e.getMessage()).sendError();
        }
    }

    @Bean
    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate;
    }
}
