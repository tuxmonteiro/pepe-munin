package com.globo.pepe.munin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globo.pepe.munin.repository.SofiaRepository;
import com.globo.pepe.munin.util.MuninConfiguration;
import java.util.List;
import java.util.Map;
import org.openstack4j.api.OSClient.OSClientV3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MuninService {

    @Autowired
    private SofiaRepository sofiaRepository;

    @Autowired
    private PepeApiService pepeApiService;

    @Autowired
    private KeystoneService keystoneService;

    @Autowired
    private MuninConfiguration configuration;


    public void send(){

        OSClientV3 os = getKeystoneService().authenticate(configuration.getUser(),configuration.getPassword(),configuration.getIdentifier());

        List<Map<String, Object>> metrics = getSofiaRepository().findByMetrics(configuration.getQueryWorker());

        if(metrics != null && !metrics.isEmpty()){
            for(Map<String, Object> metric : metrics){
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode jsonNode = mapper.valueToTree(metric);
                        getPepeApiService().sendMetrics(jsonNode,os);
            }
        }
    }

    public SofiaRepository getSofiaRepository() {
        return sofiaRepository;
    }

    public PepeApiService getPepeApiService() {
        return pepeApiService;
    }

    public KeystoneService getKeystoneService() {
        return keystoneService;
    }

}
