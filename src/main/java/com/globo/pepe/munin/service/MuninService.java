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
    private final ObjectMapper mapper;

    public MuninService(SofiaRepository sofiaRepository,
            PepeApiService pepeApiService,
            KeystoneService keystoneService,
            ObjectMapper mapper) {
        this.sofiaRepository = sofiaRepository;
        this.pepeApiService = pepeApiService;
        this.keystoneService = keystoneService;
        this.mapper = mapper;
    }

    public void send() throws Exception {
        OSClientV3 os = keystoneService.authenticate();

        final List<Map<String, Object>> metrics = sofiaRepository.findByMetrics(queryWorker);

        for(Map<String, Object> metric : metrics) {
            JsonNode jsonNode = mapper.valueToTree(metric);
            pepeApiService.sendMetrics(jsonNode,os);
        }

    }

}
