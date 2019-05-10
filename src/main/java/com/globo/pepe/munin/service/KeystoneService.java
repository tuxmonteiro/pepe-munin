package com.globo.pepe.munin.service;

import com.globo.pepe.munin.util.MuninConfiguration;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.openstack.OSFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KeystoneService {

    @Autowired
    private MuninConfiguration configuration;

    public OSClientV3 authenticate(String user, String Password, String identifier){
        OSClientV3 os = null;
        try {
             os = OSFactory.builderV3().endpoint(configuration.getKeystoneUrl())
                .credentials(user, Password,Identifier.byId(identifier)).authenticate();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return os;

    }
}
