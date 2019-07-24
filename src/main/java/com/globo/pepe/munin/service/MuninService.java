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
import com.globo.pepe.common.model.munin.Connection;
import com.globo.pepe.common.model.munin.Driver;
import com.globo.pepe.common.model.munin.Keystone;
import com.globo.pepe.common.model.munin.Metric;
import com.globo.pepe.common.model.munin.Project;
import com.globo.pepe.common.services.JsonLoggerService;
import com.globo.pepe.munin.repository.MetricRepository;
import java.util.Date;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MuninService {


    private final MetricRepository metricRepository;
    private final PepeApiService pepeApiService;
    private final KeystoneService keystoneService;
    private final ObjectMapper mapper;
    private final JsonLoggerService jsonLoggerService;
    private final JdbcProviderService jdbcProviderService;

    public MuninService(
            MetricRepository metricRepository,
            JdbcProviderService jdbcProviderService,
            PepeApiService pepeApiService,
            KeystoneService keystoneService,
            ObjectMapper mapper,
            JsonLoggerService jsonLoggerService) {
        this.metricRepository = metricRepository;
        this.jdbcProviderService = jdbcProviderService;
        this.pepeApiService = pepeApiService;
        this.keystoneService = keystoneService;
        this.mapper = mapper;
        this.jsonLoggerService = jsonLoggerService;
    }

    @Scheduled(fixedDelayString = "${pepe.munin.min-sched-delay:10000}")
    public void tick() {
        // TODO: join both queries in just one
        final List<Long> ids = metricRepository.selectAllByNeedToProcess();
        metricRepository.findByIdIn(ids).forEach(metric -> this.metricProcessing(metric));
    }

    private void metricProcessing(Metric metric) {
        try {
            metric.setLastProcessing(new Date());
            metricRepository.save(metric);

            final Project project = metric.getProject();
            final Keystone keystone = project.getKeystone();
            final Connection muninConnection = metric.getConnection();
            final String triggerName = metric.getTrigger();
            if (muninConnection == null) {
                jsonLoggerService.newLogger(getClass()).message("Munin Connection not defined").sendWarn();
                return;
            }
            if (keystoneService.authenticate(project.getName(), keystone.getLogin(), keystone.getPassword())) {
                String queryWorker = metric.getQuery();
                final List<Map<String, Object>> allTable = new ArrayList<>();
                if (Driver.Type.JDBC.equals(muninConnection.getDriver().getType())) {
                    allTable.addAll(jdbcProviderService.findByMetrics(queryWorker, muninConnection));
                }
                int count = 0;
                for (Map<String, Object> row : allTable) {
                    JsonNode metricJson = mapper.valueToTree(row);
                    String projectName = project.getName();
                    String tokenId = keystoneService.getTokenId();
                    if (pepeApiService.sendMetrics(metricJson, projectName, tokenId, triggerName)) {
                        count++;
                    }
                }
                jsonLoggerService.newLogger(getClass())
                    .message("sent " + count + "/" + allTable.size() + " events to pepe-api").sendInfo();
            }
        } catch (Exception e) {
            final String erroMsg = String.valueOf(e.getCause());
            jsonLoggerService.newLogger(getClass()).message(erroMsg).sendError();
            jsonLoggerService.newLogger(getClass()).message(erroMsg).sendDebug(e);
        }
    }

}
