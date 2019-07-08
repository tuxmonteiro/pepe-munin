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

import com.globo.pepe.common.model.munin.Connection;
import com.globo.pepe.common.services.JsonLoggerService;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class JdbcProviderService {

    private final JsonLoggerService jsonLoggerService;
    private final HikariFactoryService hikariFactoryService;

    public JdbcProviderService(
            JsonLoggerService jsonLoggerService,
            HikariFactoryService hikariFactoryService) {

        this.jsonLoggerService = jsonLoggerService;
        this.hikariFactoryService = hikariFactoryService;
    }

    public  List<Map<String, Object>> findByMetrics(String query, Connection muninConnection) {
        try {
            final List<Map<String, Object>> resultList = new ArrayList<>();
            final DataSource datasource = hikariFactoryService.dataSource(muninConnection);
            try (final java.sql.Connection connection = datasource.getConnection()) {
                final PreparedStatement pstmt = connection.prepareStatement(query);
                final ResultSet resultSet = pstmt.executeQuery();
                final ResultSetMetaData metadata = resultSet.getMetaData();
                int columns = metadata.getColumnCount();
                while (resultSet.next()) {
                    Map<String, Object> row = new HashMap<>(columns);
                    for (int columnPos = 1; columnPos <= columns; ++columnPos) {
                        row.put(metadata.getColumnName(columnPos), resultSet.getObject(columnPos));
                    }
                    resultList.add(row);
                }
            }
            return resultList;
        } catch (Exception e) {
            jsonLoggerService.newLogger(getClass()).message(e.getMessage()).sendError(e);
        }

        return Collections.emptyList();
    }

}
