package com.globo.pepe.munin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.globo.pepe.common.model.Event;
import com.globo.pepe.common.model.Metadata;
import com.globo.pepe.common.services.JsonLoggerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Calendar;

@Service
public class PepeApiService {

    @Value("${pepe.api.endpoint}")
    private String pepeApiEndpoint;

    @Value("${pepe.munin.source}")
    private String source;

    @Value("${pepe.munin.trigger_name}")
    private String triggerName;

    private final RestTemplate restTemplate;
    private final JsonLoggerService jsonLoggerService;

    public PepeApiService(JsonLoggerService jsonLoggerService) {
        this.restTemplate = new RestTemplate();
        this.jsonLoggerService = jsonLoggerService;
    }

    void sendMetrics(JsonNode metric, String project, String tokenId){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            final Event event = buildEntity(metric, project, tokenId);
            HttpEntity<Event> request = new HttpEntity<>(event, headers);
            restTemplate.exchange(pepeApiEndpoint, HttpMethod.POST, request, JsonNode.class);
        }
        catch (Exception e) {
            jsonLoggerService.newLogger(getClass()).message(e.getMessage()).sendError(e);
        }
    }

    Event buildEntity(JsonNode metric, String project, String tokenId) {
        Metadata metadata = new Metadata();
        metadata.setSource(source);
        metadata.setProject(project);
        metadata.setToken(tokenId);
        metadata.setTimestamp(Calendar.getInstance().getTimeInMillis());
        metadata.setTriggerName(triggerName);

        final Event event = new Event();
        event.setId(Long.toString(Calendar.getInstance().getTimeInMillis()));
        event.setMetadata(metadata);
        event.setPayload(metric);

        return event;
    }

}
