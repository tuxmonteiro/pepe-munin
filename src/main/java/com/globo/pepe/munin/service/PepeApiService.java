package com.globo.pepe.munin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.globo.pepe.common.model.Event;
import com.globo.pepe.common.model.Metadata;
import com.globo.pepe.common.services.JsonLoggerService;
import java.util.UUID;
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

    public PepeApiService(JsonLoggerService jsonLoggerService, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.jsonLoggerService = jsonLoggerService;
    }

    boolean sendMetrics(JsonNode metric, String project, String tokenId){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Auth-Token", tokenId);

        try {
            final Event event = buildEntity(metric, project);
            HttpEntity<Event> request = new HttpEntity<>(event, headers);
            restTemplate.exchange(pepeApiEndpoint + "/event", HttpMethod.POST, request, JsonNode.class);
            return true;
        }
        catch (Exception e) {
            jsonLoggerService.newLogger(getClass()).message(e.getMessage()).sendError(e);
        }
        return false;
    }

    Event buildEntity(JsonNode metric, String project) {
        Metadata metadata = new Metadata();
        metadata.setSource(source);
        metadata.setProject(project);
        metadata.setTimestamp(Calendar.getInstance().getTimeInMillis());
        metadata.setTriggerName(triggerName);

        final Event event = new Event();
        event.setId(source.toUpperCase() + "-" + UUID.randomUUID().toString());
        event.setMetadata(metadata);
        event.setPayload(metric);

        return event;
    }

}
