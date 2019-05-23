package com.globo.pepe.munin.service;

import com.globo.pepe.common.services.JsonLoggerService;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.api.exceptions.AuthenticationException;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.identity.v3.Project;
import org.openstack4j.model.identity.v3.Token;
import org.openstack4j.openstack.OSFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
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

    private final Object semaphore = new Object();

    private AbstractMap.Entry<OSClientV3, Date> osClientWithExpires = new SimpleImmutableEntry<>(null, new Date());

    private final JsonLoggerService loggerService;

    public KeystoneService(JsonLoggerService loggerService) {
        this.loggerService = loggerService;
    }

    boolean authenticate() throws Exception {
        synchronized (semaphore) {
            final Date currentDate = new Date();
            if (osClientWithExpires.getKey() == null || currentDate.after(osClientWithExpires.getValue())) {
                loggerService.newLogger(getClass()).message("Token expired").sendWarn();
                final OSClientV3 osClient = OSFactory.builderV3()
                        .endpoint(keystoneEndPoint)
                        .credentials(user, password, Identifier.byId(domain))
                        .scopeToProject(Identifier.byName(project), Identifier.byId(domain))
                        .authenticate();
                if (osClient == null || osClient.getToken() == null) {
                    throw new AuthenticationException("client is null", 401);
                } else {
                    final Date expires = osClient.getToken().getExpires();
                    osClientWithExpires = new SimpleImmutableEntry<>(osClient, expires);
                }
                loggerService.newLogger(getClass()).message("Using new keystone token").sendWarn();
            }
        }
        return osClientWithExpires.getKey() != null;
    }

    String getProjectName() throws Exception {
        synchronized (semaphore) {
            final OSClientV3 os = osClientWithExpires.getKey();
            final Token token;
            final Project tokenProject;
            if ((token = os.getToken()) == null || (tokenProject = token.getProject()) == null) {
                throw new AuthenticationException("token or project is null", 401);
            }
            return tokenProject.getName();
        }
    }

    String getTokenId() throws Exception {
        synchronized (semaphore) {
            final OSClientV3 os = osClientWithExpires.getKey();
            final Token token;
            if ((token = os.getToken()) == null) {
                throw new AuthenticationException("token is null", 401);
            }
            return token.getId();
        }
    }

}
