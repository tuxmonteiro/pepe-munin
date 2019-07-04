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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globo.pepe.common.services.JsonLoggerService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@WebMvcTest({
    MuninService.class,
    PepeApiService.class,
    KeystoneService.class,
    JsonLoggerService.class,
    ObjectMapper.class,
    SofiaProviderService.class
})
public class MuninServiceTest {

    @Autowired
    private MuninService muninService;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private SofiaProviderService sofiaProviderService;

    @MockBean
    private PepeApiService pepeApiService;

    private JsonNode metricValueJson = null;

    private static ClientAndServer keystoneServerMock;

    @BeforeClass
    public static void setupClass() throws IOException {
        keystoneServerMock = ClientAndServer.startClientAndServer(5000);

        InputStream resourceAuthOk = MuninServiceTest.class.getResourceAsStream("/keystone-auth.json");
        String bodyAuthOk = IOUtils.toString(resourceAuthOk, Charset.defaultCharset());
        keystoneServerMock.when(request().withMethod("POST").withPath("/v3/auth/tokens").withBody(requestBodyWithUserPassword("user", "password")))
            .respond(response().withBody(bodyAuthOk).withHeader("Content-Type", APPLICATION_JSON_VALUE).withStatusCode(201));

        InputStream resourceAuthFail = MuninServiceTest.class.getResourceAsStream("/keystone-auth-fail.json");
        String bodyAuthFail = IOUtils.toString(resourceAuthFail, Charset.defaultCharset());
        keystoneServerMock.when(request().withMethod("POST").withPath("/v3/auth/tokens").withBody(requestBodyWithUserPassword("user-wrong", "password-wrong")))
            .respond(response().withBody(bodyAuthFail).withHeader("Content-Type", APPLICATION_JSON_VALUE).withStatusCode(401));

        String bodyKeystoneError = "";
        keystoneServerMock.when(request().withMethod("POST").withPath("/v3/auth/tokens").withBody(requestBodyWithUserPassword("force-error", "")))
            .respond(response().withBody(bodyKeystoneError).withHeader("Content-Type", APPLICATION_JSON_VALUE).withStatusCode(201));

    }

    private static String requestBodyWithUserPassword(String user, String password) {
        return "{\n"
            + "  \"auth\" : {\n"
            + "    \"identity\" : {\n"
            + "      \"password\" : {\n"
            + "        \"user\" : {\n"
            + "          \"name\" : \"" + user + "\",\n"
            + "          \"domain\" : {\n"
            + "            \"id\" : \"default\"\n"
            + "          },\n"
            + "          \"password\" : \"" + password + "\"\n"
            + "        }\n"
            + "      },\n"
            + "      \"methods\" : [ \"password\" ]\n"
            + "    },\n"
            + "    \"scope\" : {\n"
            + "      \"project\" : {\n"
            + "        \"name\" : \"pepe\",\n"
            + "        \"domain\" : {\n"
            + "          \"id\" : \"default\"\n"
            + "        }\n"
            + "      }\n"
            + "    }\n"
            + "  }\n"
            + "}";
    }

    @Before
    public void setup() {
        List<Map<String, Object>> metrics = new ArrayList<>();
        Map<String, Object> metricValue = new LinkedHashMap<>();
        metricValue.put("cpu","00");
        metricValue.put("ram","00");
        metricValue.put("so","linux");
        metrics.add(metricValue);
        metricValueJson = mapper.valueToTree(metricValue);

        when(sofiaProviderService.findByMetrics(anyString())).thenReturn(metrics);
    }

    @Test
    public void sendWithUserAndPasswordOkTest() {
        muninService.send();
        verify(pepeApiService, Mockito.atLeastOnce()).sendMetrics(metricValueJson, "admin", null);
    }
}
