/*
 * Copyright (c) 2019. Globo.com - ATeam
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globo.pepe.munin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globo.pepe.common.services.JsonLoggerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MuninService {

    @Value("${pepe.munin.query}")
    private String queryWorker;

    private final SofiaProviderService sofiaProviderService;
    private final PepeApiService pepeApiService;
    private final KeystoneService keystoneService;
    private final ObjectMapper mapper;
    private final JsonLoggerService jsonLoggerService;

    public MuninService(SofiaProviderService sofiaProviderService,
        PepeApiService pepeApiService,
        KeystoneService keystoneService,
        ObjectMapper mapper,
        JsonLoggerService jsonLoggerService) {

        this.sofiaProviderService = sofiaProviderService;
        this.pepeApiService = pepeApiService;
        this.keystoneService = keystoneService;
        this.mapper = mapper;
        this.jsonLoggerService = jsonLoggerService;
    }

    @Scheduled(fixedDelayString = "${pepe.munin.fixedDelay}")
    public void send() {
        try {
            if (keystoneService.authenticate()) {
                final List<Map<String, Object>> metrics = sofiaProviderService.findByMetrics(queryWorker);

                int count = 0;
                for (Map<String, Object> metric : metrics) {
                    JsonNode metricJson = mapper.valueToTree(metric);
                    String projectName = keystoneService.getProjectName();
                    String tokenId = keystoneService.getTokenId();
                    if (pepeApiService.sendMetrics(metricJson, projectName, tokenId)) {
                        count++;
                    }
                }
                jsonLoggerService.newLogger(getClass()).message("sent " + count + "/" + metrics.size() + " events to pepe-api").sendInfo();
            }
        } catch (Exception e){
            jsonLoggerService.newLogger(getClass()).message(e.getMessage()).sendError(e);
        }
    }

}
