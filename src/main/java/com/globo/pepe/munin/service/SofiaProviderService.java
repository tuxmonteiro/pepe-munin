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

import com.globo.pepe.common.services.JsonLoggerService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SofiaProviderService {

    private final JdbcTemplate jdbcTemplate;
    private final JsonLoggerService jsonLoggerService;

    public SofiaProviderService(JsonLoggerService jsonLoggerService, JdbcTemplate jdbcTemplate) {
        this.jsonLoggerService = jsonLoggerService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public  List<Map<String, Object>> findByMetrics(String query) {
        try {
            return jdbcTemplate.queryForList(query);
        } catch (Exception e) {
            jsonLoggerService.newLogger(getClass()).message(e.getMessage()).sendError(e);
        }
        return Collections.emptyList();
    }

}
