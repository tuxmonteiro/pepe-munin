package com.globo.pepe.munin.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PepeApiService {

    RestTemplate restTemplate;

    public PepeApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendMetrics(JsonNode metric){
        System.out.println(metric.toString());
    }

    @Bean
    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate;
    }
}
