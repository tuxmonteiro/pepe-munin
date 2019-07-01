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

import com.globo.pepe.common.repository.munin.ConnectionRepository;
import com.globo.pepe.common.services.JsonLoggerService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.stereotype.Service;

@Service
public class SofiaProviderService {

    private final HikariFactoryService hikariFactoryService;
    private final ConnectionRepository connectionRepository;
    private final JsonLoggerService jsonLoggerService;

    private com.globo.pepe.common.model.munin.Connection muninConnection = null;
    private Connection dataConnection = null;

    public SofiaProviderService(JsonLoggerService jsonLoggerService, HikariFactoryService hikariFactoryService, ConnectionRepository connectionRepository) {
        this.jsonLoggerService = jsonLoggerService;
        this.hikariFactoryService = hikariFactoryService;
        this.connectionRepository = connectionRepository;
    }

    public  List<Map<String, Object>> findByMetrics(String query) {
        if (muninConnection == null) {
            System.out.println("Munin Connection not defined");
            muninConnection = connectionRepository.findAll().stream().findAny().orElse(null);
        }
        if (muninConnection != null) {
            try {
                String url = muninConnection.getUrl();
                String login = muninConnection.getLogin();
                String password = muninConnection.getPassword();
                final DataSource datasource = hikariFactoryService.dataSource(url, login, password);
                this.dataConnection = datasource.getConnection();
            } catch (Exception e) {
                this.dataConnection = null;
                System.err.println(e.getMessage());
            }
        }
        if (dataConnection != null) {
            try {
            final PreparedStatement pstmt = dataConnection.prepareStatement(query);
            final ResultSet resultSet = pstmt.executeQuery();
            final ResultSetMetaData metadata = resultSet.getMetaData();
            int columns = metadata.getColumnCount();
                final List<Map<String, Object>> resultList = new ArrayList<>();
            while (resultSet.next()){
                Map<String, Object> row = new HashMap<>(columns);
                for(int columnPos=1; columnPos<=columns; ++columnPos){
                    row.put(metadata.getColumnName(columnPos), resultSet.getObject(columnPos));
                }
                resultList.add(row);
            }
                return resultList;
            } catch (Exception e) {
                jsonLoggerService.newLogger(getClass()).message(e.getMessage()).sendError(e);
            }
        }
        return Collections.emptyList();
    }

}
