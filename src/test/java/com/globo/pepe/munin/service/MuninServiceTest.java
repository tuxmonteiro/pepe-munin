package com.globo.pepe.munin.service;

import com.globo.pepe.munin.repository.SofiaRepository;
import com.globo.pepe.munin.util.KeystoneMock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openstack4j.api.OSClient.OSClientV3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MuninServiceTest {

    @Autowired
    private MuninService muninService;

    @MockBean
    private SofiaRepository sofiaRepository;

    @MockBean
    private KeystoneService keystoneService;

    @Before
    public void initMocks(){
        List<Map<String, Object>> metrics = new ArrayList<>();
        Map<String,Object> metricValue = new LinkedHashMap<>();
        metricValue.put("cpu","00");
        metricValue.put("ram","00");
        metricValue.put("so","linux");
        metrics.add(metricValue);

        Mockito.when(this.sofiaRepository.findByMetrics(Mockito.anyString())).thenReturn(metrics);
        OSClientV3 osClientV3 = KeystoneMock.getOSClientV3();
        Mockito.when(keystoneService.authenticate()).thenReturn(osClientV3);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void  sendMetrics(){
        muninService.send();
    }

}
