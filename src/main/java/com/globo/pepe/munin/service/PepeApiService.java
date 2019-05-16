package com.globo.pepe.munin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globo.pepe.common.model.Event;
import com.globo.pepe.common.model.Metadata;
import com.globo.pepe.common.services.JsonLoggerService;
import java.util.Calendar;
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

    void sendMetrics(JsonNode metric, String project, String tokenId){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            String eventStr = buildEntity(metric, project, tokenId);
            HttpEntity<String> entity = new HttpEntity<>(eventStr, headers);
            restTemplate.exchange(pepeApiEndpoint, HttpMethod.POST, entity, String.class);
        }
        catch (Exception e) {
            jsonLoggerService.newLogger(getClass()).message(e.getMessage()).sendError();
        }
    }

    String buildEntity(JsonNode metric, String project, String tokenId) throws JsonProcessingException {
        Metadata metadata = new Metadata();
        metadata.setSource(source);
        metadata.setProject(project);
        metadata.setToken(tokenId);
        metadata.setTimestamp(Calendar.getInstance().getTimeInMillis());
        metadata.setTriggerName(project + "-acs-collector");

        final Event event = new Event();
        event.setId(Long.toString(Calendar.getInstance().getTimeInMillis()));
        event.setMetadata(metadata);
        event.setPayload(metric);

        return mapper.writeValueAsString(event);
    }

}
