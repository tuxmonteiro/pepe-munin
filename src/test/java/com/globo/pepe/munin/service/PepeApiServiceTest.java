package com.globo.pepe.munin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.globo.pepe.munin.util.KeystoneMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openstack4j.api.OSClient.OSClientV3;
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
        ObjectNode metric = mapper.createObjectNode();
        metric.put("time", "1844055562000");
        metric.put("vip_id", "00000e00-ea0c-000e-b0ac-0000f0000000");
        metric.put("vip_name", "domain.com");
        metric.put("vm_name", "vm-name");
        metric.put("vm_id", "00000e00-ea0c-000e-b0ac-0000f0000000");
        metric.put("  vm_project ", "project");
        return metric;
    }


}
