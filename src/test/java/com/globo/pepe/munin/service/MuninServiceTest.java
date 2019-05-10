package com.globo.pepe.munin.service;

import com.globo.pepe.munin.repository.SofiaRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MuninServiceTest {


    @Test
    public void  sendWithoutMetrics(){
        MuninService muninService = Mockito.mock(MuninService.class);
        SofiaRepository sofiaRepository = Mockito.mock(SofiaRepository.class);
        List<Map<String, Object>> metrics = new ArrayList<Map<String, Object>>();
        Mockito.when(sofiaRepository.findByMetrics(null)).thenReturn(metrics);
        Mockito.when(muninService.getSofiaRepository()).thenReturn(sofiaRepository);
        muninService.send();
    }

    @Test
    public void  sendMetrics(){
        try {
            MuninService muninService = Mockito.mock(MuninService.class);
            SofiaRepository sofiaRepository = Mockito.mock(SofiaRepository.class);
            PepeApiService pepeApiService = Mockito.mock(PepeApiService.class);

            List<Map<String, Object>> metrics = new ArrayList<Map<String, Object>>();
            Map<String, Object> metric = new LinkedHashMap<>();
            metric.put("name-1", "value-1");
            metric.put("name-2", "value-2");
            metric.put("name-3", "value-3");
            metric.put("name-4", "value-4");

            metrics.add(metric);
            metrics.add(metric);

            Mockito.when(sofiaRepository.findByMetrics(null)).thenReturn(metrics);
            Mockito.when(muninService.getSofiaRepository()).thenReturn(sofiaRepository);

            Mockito.when(muninService.getPepeApiService()).thenReturn(pepeApiService);
            muninService.send();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
