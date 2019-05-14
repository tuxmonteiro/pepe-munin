package com.globo.pepe.munin.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MuninConfiguration {

    private @Value("${pepe_endpoint}") String pepeApiEndpoint;
    private @Value("${keystone_url}") String keystoneEndPoint;
    private @Value("${user_keystone}") String user;
    private @Value("${password_keystone}") String password;
    private @Value("${identifier_keystone}") String identifier;
    private  @Value("${munin_query_worker}") String queryWorker;
    private @Value("${munin_source}") String source;


    public String getPepeApiEndpoint() {
        return pepeApiEndpoint;
    }

    public String getKeystoneEndPoint() {
        return keystoneEndPoint;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getQueryWorker() {
        return queryWorker;
    }

    public String getSource() {
        return source;
    }
}