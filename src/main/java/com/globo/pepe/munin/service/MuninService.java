package com.globo.pepe.munin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globo.pepe.common.services.JsonLoggerService;
import com.globo.pepe.munin.repository.SofiaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MuninService {

    @Value("${pepe.munin.query}")
    private String queryWorker;

    private final SofiaRepository sofiaRepository;
    private final PepeApiService pepeApiService;
    private final KeystoneService keystoneService;
    private final ObjectMapper mapper;
    private final JsonLoggerService jsonLoggerService;

    public MuninService(SofiaRepository sofiaRepository,
        PepeApiService pepeApiService,
        KeystoneService keystoneService,
        ObjectMapper mapper,
        JsonLoggerService jsonLoggerService) {

        this.sofiaRepository = sofiaRepository;
        this.pepeApiService = pepeApiService;
        this.keystoneService = keystoneService;
        this.mapper = mapper;
        this.jsonLoggerService = jsonLoggerService;
    }

    @Scheduled(fixedDelayString = "${pepe.munin.fixedDelay}")
    public void send() {
        try {
            if (keystoneService.authenticate()) {
                final List<Map<String, Object>> metrics = sofiaRepository.findByMetrics(queryWorker);

                int count = 0;
                for (Map<String, Object> metric : metrics) {
                    JsonNode jsonNode = mapper.valueToTree(metric);
                    boolean result = pepeApiService.sendMetrics(jsonNode, keystoneService.getProjectName(), keystoneService.getTokenId());
                    count = result ? count + 1 : count;
                }
                jsonLoggerService.newLogger(getClass()).message("sent " + count + " events to pepe-api").sendInfo();
            }
        } catch (Exception e){
            jsonLoggerService.newLogger(getClass()).message(e.getMessage()).sendError(e);
        }
    }

}
