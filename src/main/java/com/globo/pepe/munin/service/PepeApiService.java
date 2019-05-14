package com.globo.pepe.munin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.globo.pepe.common.services.JsonLoggerService;
import com.globo.pepe.munin.util.JsonNodeUtil;
import com.globo.pepe.munin.util.MuninConfiguration;
import java.util.Calendar;
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
        HttpEntity<JsonNode> entity = buildEntity(metric,osClientV3);

        try {
            restTemplate.exchange(configuration.getPepeApiEndpoint(), HttpMethod.POST, entity, JsonNode.class);
        }
        catch (Exception e) {
            jsonLoggerService.newLogger(getClass()).put("short_message", e.getMessage()).sendError();
        }
    }

    public HttpEntity<JsonNode> buildEntity(JsonNode metric, OSClientV3 osClientV3) {
        String keytoreProjectName = osClientV3.getToken().getProject().getName();

        HttpEntity<JsonNode> entity = new HttpEntity<>(metric);
        JsonNode requestBody = JsonNodeUtil.buildJsonNode();
        JsonNode metaData = JsonNodeUtil.buildJsonNode();
        ((ObjectNode) metaData).put("id", "");
        ((ObjectNode) metaData).put("source", configuration.getSource());
        ((ObjectNode) metaData).put("project", keytoreProjectName);
        ((ObjectNode) metaData).put("token", osClientV3.getToken().getId());
        ((ObjectNode) metaData).put("timestamp", Calendar.getInstance().getTimeInMillis() * 1000L * 1000L);
        ((ObjectNode) metaData).put("trigger_name", keytoreProjectName.concat("acs-collector"));
        ((ObjectNode) requestBody).put("payload", metric.toString());
        ((ObjectNode) requestBody).put("metaData", metaData.toString());
        return entity;
    }

    @Bean
    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate;
    }
}
