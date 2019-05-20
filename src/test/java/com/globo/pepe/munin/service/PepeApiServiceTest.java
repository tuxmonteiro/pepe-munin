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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PepeApiServiceTest  {

    @Autowired
    private PepeApiService pepeApiService;

    @Autowired
    private ObjectMapper mapper;

    @Before
    public void setUp(){
    }

    @Test
    public void contextLoads() {
    }

    @Test
    public void buildRequest() {
        String project = UUID.randomUUID().toString();
        String tokenId = UUID.randomUUID().toString();
        JsonNode metric = getMetricMock();
        final Event event = pepeApiService.buildEntity(metric, project, tokenId);
        //TODO: test event
    }

    @Test
    public void sendMetrics(){
        String project = UUID.randomUUID().toString();
        String tokenId = UUID.randomUUID().toString();
        JsonNode metric = getMetricMock();
        pepeApiService.sendMetrics(metric, project, tokenId);
        //TODO: MockServer response from restTemplate.exchange()
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
