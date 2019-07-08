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

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.globo.pepe.common.services.JsonLoggerService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service
public class HikariFactoryService {

    private final JsonLoggerService loggerService;

    private final LoadingCache<com.globo.pepe.common.model.munin.Connection, DataSource> cache;

    public HikariFactoryService(JsonLoggerService loggerService,
                                @Value("${pepe.munin.jdbc.datasource-cache-ttl}") Long dataSourceCacheTtl) {
        this.loggerService = loggerService;
        this.cache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(dataSourceCacheTtl, TimeUnit.MILLISECONDS)
                .removalListener((key, value, cause) -> {
                    if (value instanceof HikariDataSource) {
                        try {
                            final String logMsg = "Closing " +
                                    ((com.globo.pepe.common.model.munin.Connection) Objects.requireNonNull(key)).getUrl();
                            loggerService.newLogger(getClass()).message(logMsg).sendWarn();
                            ((HikariDataSource) value).close();
                        } catch (Exception ignored) {
                            // ignored
                        }
                    }
                }).build(this::newDataSource);
    }

    private DataSource newDataSource(com.globo.pepe.common.model.munin.Connection muninConnection) {
        String url = muninConnection.getUrl();
        String login = muninConnection.getLogin();
        String password = muninConnection.getPassword();
        loggerService.newLogger(getClass()).message("Building a new datasource (" + url.split("[?]")[0] + ")").sendInfo();
        return new HikariDataSource(getHikariConfig(url, login, password));
    }

    public DataSource dataSource(com.globo.pepe.common.model.munin.Connection connection) {
        return cache.get(connection);
    }

    private HikariConfig getHikariConfig(String jdbcUrl, String username, String password) {
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        final String[] jdbcUrlSplit = jdbcUrl.split("[?]");
        if (jdbcUrlSplit.length > 1) {
            Stream.of(jdbcUrlSplit[1].split("&")).forEach(queryStr -> {
                final String[] queryStrKeyValue = queryStr.split("=");
                String key = queryStrKeyValue[0];
                String value = "";
                if (queryStrKeyValue.length > 1) {
                    try {
                        value = URLDecoder.decode(queryStrKeyValue[1], StandardCharsets.UTF_8.name());
                    } catch (UnsupportedEncodingException ignore) {
                        // ignored
                    }
                }
                config.addDataSourceProperty(key, value);
            });
        }
        final Properties dataSourceProperties = config.getDataSourceProperties();
        if (dataSourceProperties.get("cachePrepStmts") == null) {
            config.addDataSourceProperty("cachePrepStmts", "true");
        }
        if (dataSourceProperties.get("prepStmtCacheSize") == null) {
            config.addDataSourceProperty("prepStmtCacheSize", "250");
        }
        if (dataSourceProperties.get("prepStmtCacheSqlLimit") == null) {
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        }
        return config;
    }

}
