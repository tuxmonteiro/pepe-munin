package com.globo.pepe.munin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.globo.pepe.munin.repository.SofiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MuninService {

    @Autowired
    private SofiaRepository sofiaRepository;

    @Autowired
    private PepeApiService pepeApiService;

    public void send(){
       JsonNode metric = sofiaRepository.findByMetrics();
        pepeApiService.sendMetrics(metric);
    }
}
