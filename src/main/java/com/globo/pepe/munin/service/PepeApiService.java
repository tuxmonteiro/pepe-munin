package com.globo.pepe.munin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.globo.pepe.munin.util.MuninConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PepeApiService {

    RestTemplate restTemplate;

    @Autowired
    private MuninConfiguration configuration;

    public PepeApiService() {
        this.restTemplate = getRestTemplate();
    }

    public void sendMetrics(JsonNode metric){
        HttpEntity<JsonNode> entity = new HttpEntity<>(metric);

        try {
            restTemplate.exchange(configuration.getPepeApiEndpoint(), HttpMethod.POST, entity, JsonNode.class);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Bean
    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate;
    }
}
