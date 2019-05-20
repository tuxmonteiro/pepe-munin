package com.globo.pepe.munin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.UUID;
import com.globo.pepe.common.model.Event;
import org.junit.Before;
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

    @Before
    public void setUp(){
    }

    @Test
    public void contextLoads() {
    }

    @Test
    public void buildRequestTest() {
        String project = UUID.randomUUID().toString();
        String tokenId = UUID.randomUUID().toString();
        JsonNode metric = getMetricMock();
        final Event event = pepeApiService.buildEntity(metric, project, tokenId);
        assertEquals(event.getMetadata().getProject(), project);
        assertEquals(event.getMetadata().getToken(),tokenId);
        assertEquals(event.getPayload(), metric);
    }

    @Test
    public void sendMetricsTest() {
        String project = UUID.randomUUID().toString();
        String tokenId = UUID.randomUUID().toString();
        JsonNode metric = getMetricMock();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final Event event = new Event();

        when(pepeApiService.buildEntity(metric, project, tokenId)).thenReturn(event);

        HttpEntity<Event> request = new HttpEntity<>(event, headers);

        pepeApiService.sendMetrics(metric, project, tokenId);
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
