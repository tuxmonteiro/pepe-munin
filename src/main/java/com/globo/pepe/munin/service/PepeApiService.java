package com.globo.pepe.munin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.globo.pepe.common.services.JsonLoggerService;
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
    private final ObjectMapper mapper;

    public PepeApiService(JsonLoggerService jsonLoggerService, ObjectMapper mapper) {
        this.mapper = mapper;
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
            jsonLoggerService.newLogger(getClass()).message(e.getMessage()).sendError();
        }
    }

    public JsonNode buildEntity(JsonNode metric, OSClientV3 osClientV3) {
        String keytoreProjectName = osClientV3.getToken().getProject().getName();
        ObjectNode requestBody = mapper.createObjectNode();
        ObjectNode metaData = mapper.createObjectNode();
        metaData.put("id", Calendar.getInstance().getTimeInMillis());
        metaData.put("source", source);
        metaData.put("project", keytoreProjectName);
        metaData.put("token", osClientV3.getToken().getId());
        metaData.put("timestamp", Calendar.getInstance().getTimeInMillis() * 1000L * 1000L);
        metaData.put("trigger_name", keytoreProjectName.concat("acs-collector"));
        requestBody.set("payload", metric);
        requestBody.set("metadata", metaData);
        requestBody.put("id", "xxxx"+Calendar.getInstance().getTimeInMillis());
        return requestBody;
    }

}
