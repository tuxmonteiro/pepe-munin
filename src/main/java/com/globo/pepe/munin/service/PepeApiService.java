/*
 * Copyright (c) 2019. Globo.com - ATeam
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    private final RestTemplate restTemplate;
    private final JsonLoggerService jsonLoggerService;

    public PepeApiService(JsonLoggerService jsonLoggerService, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.jsonLoggerService = jsonLoggerService;
    }

    boolean sendMetrics(JsonNode metric, String project, String tokenId, String triggerName){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Auth-Token", tokenId);

        try {
            final Event event = buildEntity(metric, project, triggerName);
            HttpEntity<Event> request = new HttpEntity<>(event, headers);
            restTemplate.exchange(pepeApiEndpoint + "/event", HttpMethod.POST, request, JsonNode.class);
            return true;
        }
        catch (Exception e) {
            jsonLoggerService.newLogger(getClass()).message(e.getMessage()).sendError(e);
        }
        return false;
    }

    Event buildEntity(JsonNode metric, String project, String triggerName) {
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
