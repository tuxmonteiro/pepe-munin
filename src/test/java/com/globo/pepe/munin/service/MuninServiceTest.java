package com.globo.pepe.munin.service;

import com.globo.pepe.common.services.JsonLoggerService;
import com.globo.pepe.munin.repository.SofiaRepository;
import com.globo.pepe.munin.util.KeystoneMock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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
        MuninService muninService = Mockito.mock(MuninService.class);
        SofiaRepository sofiaRepository = Mockito.mock(SofiaRepository.class);
        List<Map<String, Object>> metrics = new ArrayList<Map<String, Object>>();
        Mockito.when(sofiaRepository.findByMetrics(Mockito.anyString())).thenReturn(metrics);
        Mockito.when(muninService.getSofiaRepository()).thenReturn(sofiaRepository);
        muninService.send();
    }

}
