package com.globo.pepe.munin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globo.pepe.munin.repository.SofiaRepository;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MuninService {

    @Autowired
    private SofiaRepository sofiaRepository;

    @Autowired
    private PepeApiService pepeApiService;

    public void send(){
        List<Map<String, Object>> metrics = getSofiaRepository().findByMetrics();
        if(metrics != null && !metrics.isEmpty()){
            for(Map<String, Object> metric : metrics){
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode jsonNode = mapper.valueToTree(metric);
                        getPepeApiService().sendMetrics(jsonNode);
            }
        }

    }

    public SofiaRepository getSofiaRepository() {
        return sofiaRepository;
    }

    public PepeApiService getPepeApiService() {
        return pepeApiService;
    }
}
