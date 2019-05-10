package com.globo.pepe.munin.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MuninConfiguration {

    private @Value("${pepe_endpoint}") String pepeApiEndpoint;

    private @Value("${keystone_url}") String keystoneUrl;
    private @Value("${keystone_domain}") String keystoneDomainContext;
    private @Value("${pepe_security_disabled}") Boolean securityDisabled;
    private @Value("${keystone_project}") String project;
    private @Value("${keystone_token}") String token;
    private @Value("${user_keystone}") String user;
    private @Value("${password_keystone}") String password;
    private @Value("${identifier_keystone}") String identifier;

    public String getPepeApiEndpoint() {
        return pepeApiEndpoint;
    }

    public String getKeystoneUrl() {
        return keystoneUrl;
    }

    public String getKeystoneDomainContext() {
        return keystoneDomainContext;
    }

    public Boolean getSecurityDisabled() {
        return securityDisabled;
    }

    public String getProject() {
        return project;
    }

    public String getToken() {
        return token;
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
}