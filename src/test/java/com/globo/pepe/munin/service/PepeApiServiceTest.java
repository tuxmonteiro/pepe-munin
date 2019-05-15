package com.globo.pepe.munin.service;

import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.globo.pepe.munin.util.JsonNodeUtil;
import com.globo.pepe.munin.util.KeystoneMock;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openstack4j.api.OSClient.OSClientV3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PepeApiServiceTest  {

    @Autowired
    private PepeApiService pepeApiService;

    @Before
    public void setUp(){
    }

    @Test
    public void contextLoads() {
    }

    @Test
    public void  getRestTemplate(){
       RestTemplate restTemplate =  pepeApiService.getRestTemplate();
        assertThat(restTemplate, Matchers.notNullValue());
    }

    @Test(expected = NullPointerException.class)
    public void sendMetricsWithoutParametersNull(){
        pepeApiService.sendMetrics(null,null);
    }

    @Test
    public void buildRequest(){
        OSClientV3 osClientV3 = Mockito.mock(OSClientV3.class);
        Mockito.when(osClientV3.getToken()).thenReturn(KeystoneMock.getToken());
        JsonNode metric = getMetricMock();
        pepeApiService.buildEntity(metric,osClientV3);
    }


    @Test
    public void sendMetrics(){
        OSClientV3 osClientV3 = Mockito.mock(OSClientV3.class);
        Mockito.when(osClientV3.getToken()).thenReturn(KeystoneMock.getToken());
        JsonNode metric = getMetricMock();
        pepeApiService.sendMetrics(metric,osClientV3);
    }

    private JsonNode getMetricMock() {
        JsonNode metric = JsonNodeUtil.buildJsonNode();
        ((ObjectNode) metric).put("time", "1844055562000");
        ((ObjectNode) metric).put("vip_id", "00000e00-ea0c-000e-b0ac-0000f0000000");
        ((ObjectNode) metric).put("vip_name", "domain.com");
        ((ObjectNode) metric).put("vm_name", "vm-name");
        ((ObjectNode) metric).put("vm_id", "00000e00-ea0c-000e-b0ac-0000f0000000");
        ((ObjectNode) metric).put("  vm_project ", "project");
        return metric;
    }


}
