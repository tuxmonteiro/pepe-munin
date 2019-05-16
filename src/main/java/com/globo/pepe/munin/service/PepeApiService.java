package com.globo.pepe.munin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.globo.pepe.common.services.JsonLoggerService;
import com.globo.pepe.munin.util.JsonNodeUtil;
import com.globo.pepe.munin.util.MuninConfiguration;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.openstack4j.api.OSClient.OSClientV3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PepeApiService {

    RestTemplate restTemplate;

    private final JsonLoggerService jsonLoggerService;

    @Autowired
    private MuninConfiguration configuration;

    public PepeApiService(JsonLoggerService jsonLoggerService) {
        this.restTemplate = getRestTemplate();
        this.jsonLoggerService = jsonLoggerService;
    }

    public void sendMetrics(JsonNode metric, OSClientV3 osClientV3){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JsonNode obj =  buildEntity(metric,osClientV3);
        HttpEntity<String> entity = new HttpEntity<>(obj.toString(),headers);

        try {
            restTemplate.exchange(configuration.getPepeApiEndpoint(), HttpMethod.POST, entity, String.class);
        }
        catch (Exception e) {
            jsonLoggerService.newLogger(getClass()).put("short_message", e.getMessage()).sendError();
        }
    }

    public JsonNode buildEntity(JsonNode metric, OSClientV3 osClientV3) {
        String keytoreProjectName = osClientV3.getToken().getProject().getName();
        JsonNode requestBody = JsonNodeUtil.buildJsonNode();
        JsonNode metaData = JsonNodeUtil.buildJsonNode();
        ((ObjectNode) metaData).put("id", Calendar.getInstance().getTimeInMillis());
        ((ObjectNode) metaData).put("source", configuration.getSource());
        ((ObjectNode) metaData).put("project", keytoreProjectName);
        ((ObjectNode) metaData).put("token", osClientV3.getToken().getId());
        ((ObjectNode) metaData).put("timestamp", Calendar.getInstance().getTimeInMillis() * 1000L * 1000L);
        ((ObjectNode) metaData).put("trigger_name", keytoreProjectName.concat("acs-collector"));
        ((ObjectNode) requestBody).set("payload", metric);
        ((ObjectNode) requestBody).set("metadata", metaData);
        ((ObjectNode) requestBody).put("id", "xxxx"+Calendar.getInstance().getTimeInMillis());
        return requestBody;
    }

    @Bean
    public RestTemplate getRestTemplate() {
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate;
    }

}
