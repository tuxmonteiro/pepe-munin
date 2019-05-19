package com.globo.pepe.munin.service;

import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.api.exceptions.AuthenticationException;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.openstack.OSFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.Date;

@Service
public class KeystoneService {

    @Value("${pepe.keystone.url}")
    private String keystoneEndPoint;

    @Value("${pepe.keystone.user}")
    private String user;

    @Value("${pepe.keystone.password}")
    private String password;

    @Value("${pepe.keystone.domain}")
    private String domain;

    @Value("${pepe.keystone.project}")
    private String project;

    private AbstractMap.Entry<OSClientV3, Date> osClientWithExpires = new AbstractMap.SimpleEntry<>(null, new Date());

    public synchronized OSClientV3 authenticate() throws Exception {
        final Date currentDate = new Date();
        if (osClientWithExpires.getKey() == null || currentDate.after(osClientWithExpires.getValue())) {
            final OSClientV3 osClient = OSFactory.builderV3()
                    .endpoint(keystoneEndPoint)
                    .credentials(user, password, Identifier.byId(domain))
                    .scopeToProject(Identifier.byName(project), Identifier.byId(domain))
                    .authenticate();
            if (osClient == null || osClient.getToken() == null) {
                throw new AuthenticationException("client is null", 401);
            } else {
                final Date expires = osClient.getToken().getExpires();
                osClientWithExpires = new AbstractMap.SimpleImmutableEntry<>(osClient, expires);
            }
        }
        return osClientWithExpires.getKey();
    }

}
