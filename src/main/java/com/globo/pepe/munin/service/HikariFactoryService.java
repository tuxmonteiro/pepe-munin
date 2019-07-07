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

import com.globo.pepe.common.model.munin.Driver;
import com.globo.pepe.common.model.munin.Driver.Type;
import com.globo.pepe.common.services.JsonLoggerService;
import com.globo.pepe.munin.repository.DriverRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class HikariFactoryService {

    private final JsonLoggerService loggerService;

    private final Long dataSourceCacheTtl;

    private class CacheDB {

        private final Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();
        private final Map<String, Long> ttlMap = new ConcurrentHashMap<>();

        synchronized DataSource get(String key) {
            checkTtl(key);
            return dataSourceMap.get(key);
        }

        synchronized DataSource put(String key, DataSource dataSource) {
            ttlMap.put(key, System.currentTimeMillis());
            return dataSourceMap.put(key, dataSource);
        }

        private void checkTtl(String key) {
            final Long ttl;
            if ((ttl = ttlMap.get(key)) != null &&
                 ttl < System.currentTimeMillis() - Optional.ofNullable(dataSourceCacheTtl).orElse(10000L)) {

                ttlMap.remove(key);
                final HikariDataSource dataSource = (HikariDataSource) dataSourceMap.remove(key);
                dataSource.close();
            }
        }
    }

    private static class DriverShim implements java.sql.Driver {

        private final java.sql.Driver driver;

        DriverShim(java.sql.Driver driver) {
            this.driver = driver;
        }

        java.sql.Driver getRealDriver() {
            return driver;
        }

        @Override
        public Connection connect(String s, Properties properties) throws SQLException {
            return driver.connect(s, properties);
        }

        @Override
        public boolean acceptsURL(String s) throws SQLException {
            return driver.acceptsURL(s);
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String s, Properties properties) throws SQLException {
            return driver.getPropertyInfo(s, properties);
        }

        @Override
        public int getMajorVersion() {
            return driver.getMajorVersion();
        }

        @Override
        public int getMinorVersion() {
            return driver.getMinorVersion();
        }

        @Override
        public boolean jdbcCompliant() {
            return driver.jdbcCompliant();
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return driver.getParentLogger();
        }
    }

    private final CacheDB cacheDB = new CacheDB();

    private final DriverRepository driverRepository;

    public HikariFactoryService(JsonLoggerService loggerService,
                                DriverRepository driverRepository,
                                @Value("${pepe.munin.jdbc.datasource-cache-ttl}") Long dataSourceCacheTtl) {
        this.loggerService = loggerService;
        this.dataSourceCacheTtl = dataSourceCacheTtl;
        this.driverRepository = driverRepository;
    }

    @Scheduled(fixedDelayString = "${pepe.munin.jdbc.register-sched-delay}")
    public void syncDriverRegistered() {
        final List<Driver> muninDriverList = driverRepository.findByTypeAndJarNotNull(Type.JDBC);
        final List<java.sql.Driver> sqlDrivers = DriverManager.drivers().collect(Collectors.toList());
        final Map<String, Object> driversRegisteredMap = extractDriverMap(sqlDrivers);
        registerNewDrivers(muninDriverList, driversRegisteredMap);
        deregisterObsoleteDrivers(muninDriverList, driversRegisteredMap);
    }

    private void deregisterObsoleteDrivers(final List<Driver> muninDriverList, final Map<String, Object> driversRegisteredMap) {
        final List<String> driversToRemove = new ArrayList<>(driversRegisteredMap.keySet());
        final Set<String> muninDriversName = extractDriverMap(muninDriverList).keySet();
        driversToRemove.removeAll(muninDriversName);
        driversToRemove.stream().filter(driversRegisteredMap::containsKey).forEach(className -> {
            java.sql.Driver driver = (java.sql.Driver) driversRegisteredMap.get(className);
            if (driver instanceof DriverShim && ((DriverShim)driver).getRealDriver().getClass().getName().equals(className)) {
                try {
                    DriverManager.deregisterDriver(driver);
                    loggerService.newLogger(getClass())
                            .message(className + " driver deregistered").sendInfo();
                } catch (SQLException e) {
                    loggerService.newLogger(getClass()).message(String.valueOf(e.getCause())).sendError(e);
                }
            }
        });
    }

    private void registerNewDrivers(final List<Driver> muninDriverList, final Map<String, Object> driversRegisteredMap) {
        muninDriverList.forEach(driver -> {
            final String driverClassName = driver.getName();
            final String driverJar = driver.getJar();
            if (!driversRegisteredMap.keySet().contains(driverClassName)) {
                try {
                    DriverManager.registerDriver(convertToSqlDriver(driverClassName, driverJar));
                    loggerService.newLogger(getClass())
                            .message(driverClassName + "@" + driverJar + " driver registered").sendInfo();
                } catch (SQLException e) {
                    loggerService.newLogger(getClass()).message(String.valueOf(e.getCause())).sendError();
                }
            }
        });
    }

    public synchronized DataSource dataSource(String jdbcUrl, String username, String password) {
        final DataSource dataSource;
        String dataSourceKey = Base64.getEncoder().encodeToString((jdbcUrl + username + password).getBytes());
        if ((dataSource = cacheDB.get(dataSourceKey)) != null) {
            return dataSource;
        }
        return cacheDB.put(dataSourceKey, new HikariDataSource(getHikariConfig(jdbcUrl, username, password)));
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

    private DriverShim convertToSqlDriver(String driverClassName, String driverJar) {
        Assert.hasText(driverClassName, "Driver 'name' must not be null or empty!");
        Assert.hasText(driverJar, "Driver 'jar' must not be null or empty!");

        DriverShim driverShim = null;
        String driverLibJarPath = getDriverLibPath(driverJar);
        try {
            final URL urlJarPath = new URL(driverLibJarPath);
            final URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{urlJarPath});
            final java.sql.Driver driver = (java.sql.Driver)
                    Class.forName(driverClassName, true, urlClassLoader).getDeclaredConstructor().newInstance();
            driverShim = new DriverShim(driver);
        } catch (Exception e) {
            loggerService.newLogger(getClass()).message(String.valueOf(e.getCause())).sendError(e);
        }

        return driverShim;
    }

    private String getDriverLibPath(String driverJar) {
        return "jar:file:" + driverJar + "!/";
    }

    private Map<String, Object> extractDriverMap(final List<?> drivers) {
        final Map<String, Object> driverMap = new HashMap<>();
        drivers.forEach(driver -> {
            if (driver instanceof java.sql.Driver) {
                String driverName = getRealClassName((java.sql.Driver) driver);
                loggerService.newLogger(getClass()).message(driverName + " already loaded").sendDebug();
                driverMap.put(driverName, driver);
            } else if (driver instanceof Driver) {
                driverMap.put(((Driver)driver).getName(), driver);
            }
        });
        return driverMap;
    }

    private String getRealClassName(java.sql.Driver sqlDriver) {
        String driverName;
        final Class<? extends java.sql.Driver> driverClass = sqlDriver.getClass();
        if (DriverShim.class.getName().equals(driverClass.getName())) {
            driverName = ((DriverShim) sqlDriver).getRealDriver().getClass().getName();
        } else {
            driverName = driverClass.getName();
        }
        return driverName;
    }

}
