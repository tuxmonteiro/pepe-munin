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
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class HikariFactoryService {

    private final JsonLoggerService loggerService;

    private static class DriverShim implements java.sql.Driver {
        private final java.sql.Driver driver;

        DriverShim(java.sql.Driver driver) {
            this.driver = driver;
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

    private final DriverRepository driverRepository;
    private final Map<String, java.sql.Driver> driversRegistered = new HashMap<>();

    public HikariFactoryService(JsonLoggerService loggerService,
        DriverRepository driverRepository) {
        this.loggerService = loggerService;
        this.driverRepository = driverRepository;
    }

    @Scheduled(fixedRate = 10_000)
    public void syncDriverRegistered() {
        final List<com.globo.pepe.common.model.munin.Driver> driverList = driverRepository.findByType(Type.JDBC);

        loggerService.newLogger(getClass())
            .message("Drivers: registered -> " + driversRegistered.size() + " / persisted (DB) -> " + driverList.size()).sendInfo();
        driverList.forEach(driver -> {
            String driverClassName = driver.getName();
            if (!driversRegistered.keySet().contains(driverClassName)) {
                DriverShim driverShim = registerDriver(new SimpleImmutableEntry<>(driverClassName, driver.getJar()));
                driversRegistered.put(driverClassName, driverShim);
                loggerService.newLogger(getClass())
                    .message(driverClassName + "/" + driver.getJar() + " driver registered").sendInfo();
            }
        });
        final Set<String> driversToRemove = new HashSet<>(driversRegistered.keySet());
        driversToRemove.removeAll(driverList.stream().map(Driver::getName).collect(Collectors.toSet()));
        driversToRemove.forEach(className -> {
            try {
                DriverManager.deregisterDriver(driversRegistered.remove(className));
            } catch (SQLException e) {
                loggerService.newLogger(getClass())
                    .message(e.getMessage()).sendError();
            }
        });
    }

    public DataSource dataSource(String jdbcUrl, String username, String password) {
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return new HikariDataSource(config);
    }

    private DriverShim registerDriver(Entry<String, String> driverEntries) {
        final String driverClassName = driverEntries.getKey();
        final String driverJar = driverEntries.getValue();

        Assert.hasText(driverClassName, "Driver 'class_name' must not be null or empty!");
        Assert.hasText(driverJar, "Driver 'jar' must not be null or empty!");

        DriverShim driverShim = null;
        try {
            String driverLibJarPath = getDriverLibPath(driverJar);
            URL urlJarPath = new URL(driverLibJarPath);
            URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{urlJarPath});
            java.sql.Driver driver = (java.sql.Driver) Class.forName(driverClassName, true, urlClassLoader).getDeclaredConstructor().newInstance();
            driverShim = new DriverShim(driver);
            DriverManager.registerDriver(driverShim);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return driverShim;
    }

    private String getDriverLibPath(String driverJar) {
        return "jar:file:" + driverJar + "!/";
    }

}
