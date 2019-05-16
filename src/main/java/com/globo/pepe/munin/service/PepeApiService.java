package com.globo.pepe.munin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.globo.pepe.common.services.JsonLoggerService;
import com.globo.pepe.munin.util.JsonNodeUtil;
import java.util.Calendar;
import org.openstack4j.api.OSClient.OSClientV3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PepeApiService {

    @Value("${pepe.api.endpoint}")
    private String pepeApiEndpoint;

    @Value("${pepe.munin.source}")
    private String source;

    private final RestTemplate restTemplate;
    private final JsonLoggerService jsonLoggerService;

    public PepeApiService(JsonLoggerService jsonLoggerService) {
        this.restTemplate = new RestTemplate();
        this.jsonLoggerService = jsonLoggerService;
    }

    public void sendMetrics(JsonNode metric, OSClientV3 osClientV3){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JsonNode obj =  buildEntity(metric,osClientV3);
        HttpEntity<String> entity = new HttpEntity<>(obj.toString(),headers);

        try {
            restTemplate.exchange(pepeApiEndpoint, HttpMethod.POST, entity, String.class);
        }
        catch (Exception e) {
            jsonLoggerService.newLogger(getClass()).put("short_message", e.getMessage()).sendError();
        }
    }

    public JsonNode buildEntity(JsonNode metric, OSClientV3 osClientV3) {
        String keytoreProjectName = osClientV3.getToken().getProject().getName();
        JsonNode requestBody = JsonNodeUtil.buildJsonNode();
        JsonNode metaData = JsonNodeUtil.buildJsonNode();
        ((ObjectNode) metaData).put("id", Calendar.getInstance().getTimeInMillis());
        ((ObjectNode) metaData).put("source", source);
        ((ObjectNode) metaData).put("project", keytoreProjectName);
        ((ObjectNode) metaData).put("token", osClientV3.getToken().getId());
        ((ObjectNode) metaData).put("timestamp", Calendar.getInstance().getTimeInMillis() * 1000L * 1000L);
        ((ObjectNode) metaData).put("trigger_name", keytoreProjectName.concat("acs-collector"));
        ((ObjectNode) requestBody).set("payload", metric);
        ((ObjectNode) requestBody).set("metadata", metaData);
        ((ObjectNode) requestBody).put("id", "xxxx"+Calendar.getInstance().getTimeInMillis());
        return requestBody;
    }

}
