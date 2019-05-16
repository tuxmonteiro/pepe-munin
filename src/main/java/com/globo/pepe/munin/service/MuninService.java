package com.globo.pepe.munin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globo.pepe.munin.repository.SofiaRepository;
import java.util.List;
import java.util.Map;
import org.openstack4j.api.OSClient.OSClientV3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MuninService {

    @Value("${pepe.munin.query}")
    private String queryWorker;

    private final SofiaRepository sofiaRepository;
    private final PepeApiService pepeApiService;
    private final KeystoneService keystoneService;

    public MuninService(SofiaRepository sofiaRepository,
            PepeApiService pepeApiService,
            KeystoneService keystoneService) {
        this.sofiaRepository = sofiaRepository;
        this.pepeApiService = pepeApiService;
        this.keystoneService = keystoneService;
    }

    public void send(){
        OSClientV3 os = keystoneService.authenticate();

        List<Map<String, Object>> metrics = sofiaRepository.findByMetrics(queryWorker);

        if(metrics != null && !metrics.isEmpty()){
            for(Map<String, Object> metric : metrics){
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode jsonNode = mapper.valueToTree(metric);
                pepeApiService.sendMetrics(jsonNode,os);
            }
        }
    }

}
