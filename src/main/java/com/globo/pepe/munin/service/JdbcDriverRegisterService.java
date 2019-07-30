/*
 * Copyright (c) 2019. Globo.com - ATeam
 * All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
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
import com.globo.pepe.common.services.JsonLoggerService;
import com.globo.pepe.munin.configuration.HttpClientConfiguration.HttpClient;
import com.globo.pepe.munin.repository.DriverRepository;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class JdbcDriverRegisterService {

    private final JsonLoggerService loggerService;
    private final DriverRepository driverRepository;
    private final HttpClient client;
    private final String jarLocalDir;

    public JdbcDriverRegisterService(
        JsonLoggerService loggerService,
        DriverRepository driverRepository,
        HttpClient client,
        @Value("${pepe.munin.jar-local-dir:/tmp}") String jarLocalDir) {

        this.loggerService = loggerService;
        this.driverRepository = driverRepository;
        this.client = client;
        this.jarLocalDir = jarLocalDir != null ? (!jarLocalDir.endsWith("/") ? jarLocalDir + "/" : jarLocalDir) : "/tmp/";
    }

    @Scheduled(fixedDelayString = "${pepe.munin.jdbc.register-sched-delay}")
    public void syncRepositoryToDriverManager() {
        final List<Driver> muninDriverList = driverRepository.findByTypeAndJarNotNull(Driver.Type.JDBC);
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
            String driverJar = driver.getJar();
            String localJarStr = driverJar.startsWith("http") ? jarLocalDir + driverJar.substring(driverJar.lastIndexOf("/") + 1) : jarLocalDir;
            Path localJar = Paths.get(localJarStr);
            try {
                if (!localJar.toFile().exists()) {
                    client.getAndSave(driverJar, localJarStr);
                }
                if (!driversRegisteredMap.keySet().contains(driverClassName)) {
                    try {
                        DriverManager.registerDriver(convertToSqlDriver(driverClassName, localJarStr));
                        loggerService.newLogger(getClass())
                            .message(driverClassName + "@" + localJarStr + " driver registered").sendInfo();
                    } catch (SQLException e) {
                        loggerService.newLogger(getClass()).message(String.valueOf(e.getCause())).sendError();
                    }
                }
            } catch (Exception e) {
                loggerService.newLogger(getClass()).message("Error when loading " + driverJar + " (" + localJarStr + ") with error: " + e.getCause()).sendError();
            }
        });
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
}
