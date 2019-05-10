package com.globo.pepe.munin.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MuninConfiguration {

    private @Value("${pepe_endpoint}") String pepeApiEndpoint;

    public String getPepeApiEndpoint() {
        return pepeApiEndpoint;
    }
}
