package com.globo.pepe.munin.service;

import static org.junit.Assert.assertThat;

import com.globo.pepe.munin.service.SofiaProviderService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SofiaProviderServiceTests {

    @MockBean
    private SofiaProviderService sofiaRepository;

    @Test
    public void findByMetricsTest() {
        List<Map<String, Object>> metrics = new ArrayList<>();
        Map<String,Object> metricValue = new LinkedHashMap<>();
        metricValue.put("cpu","00");
        metricValue.put("ram","00");
        metricValue.put("so","linux");
        metrics.add(metricValue);
        Mockito.when(sofiaRepository.findByMetrics(Mockito.anyString())).thenReturn(metrics);
        metrics = sofiaRepository.findByMetrics(Mockito.anyString());
        assertThat(metrics, Matchers.notNullValue());

    }



}
