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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.UUID;
import com.globo.pepe.common.model.Event;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = {
        "pepe.api.endpoint=http://127.0.0.1:8000"
})
public class PepeApiServiceTest  {

    @SpyBean
    private PepeApiService pepeApiService;

    @MockBean
    private RestTemplate restTemplate;

    @Value("${pepe.api.endpoint}")
    private String pepeApiEndpoint;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void buildRequestTest() {
        String project = UUID.randomUUID().toString();
        JsonNode metric = getMetricMock();
        String triggerName = "acs-collector";
        final Event event = pepeApiService.buildEntity(metric, project, triggerName);
        assertEquals(event.getMetadata().getProject(), project);
        assertEquals(event.getPayload(), metric);
    }

    @Test
    public void sendMetricsTest() {
        String project = UUID.randomUUID().toString();
        String tokenId = UUID.randomUUID().toString();
        JsonNode metric = getMetricMock();
        String triggerName = "acs-collector";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Auth-Token", tokenId);

        final Event event = new Event();

        when(pepeApiService.buildEntity(metric, project, triggerName)).thenReturn(event);

        HttpEntity<Event> request = new HttpEntity<>(event, headers);

        pepeApiService.sendMetrics(metric, project, tokenId, triggerName);
        verify(restTemplate, Mockito.atLeastOnce()).exchange(pepeApiEndpoint + "/event", HttpMethod.POST, request, JsonNode.class);
    }

    private JsonNode getMetricMock() {
        ObjectNode metric = mapper.createObjectNode();
        metric.put("time", "1844055562000");
        metric.put("vip_id", "00000e00-ea0c-000e-b0ac-0000f0000000");
        metric.put("vip_name", "domain.com");
        metric.put("vm_name", "vm-name");
        metric.put("vm_id", "00000e00-ea0c-000e-b0ac-0000f0000000");
        metric.put("vm_project ", "project");
        return metric;
    }


}
