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

import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.globo.pepe.common.model.munin.Connection;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SofiaProviderServiceTests {

    @MockBean
    private SofiaProviderService sofiaRepository;

    @Test
    public void findByMetricsTest() {
        List<Map<String, Object>> metrics = new ArrayList<>();
        Map<String,Object> metricValue = new LinkedHashMap<>();
        metricValue.put("cpu","00");
        metricValue.put("ram","00");
        metricValue.put("so","linux");
        metrics.add(metricValue);
        Mockito.when(sofiaRepository.findByMetrics(anyString(), any(Connection.class))).thenReturn(metrics);
        metrics = sofiaRepository.findByMetrics(anyString(), any(Connection.class));
        assertThat(metrics, Matchers.notNullValue());
    }



}
