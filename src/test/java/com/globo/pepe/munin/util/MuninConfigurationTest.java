package com.globo.pepe.munin.util;

import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MuninConfigurationTest  {

    @Autowired
    private MuninConfiguration configuration;

    @Test
    public void contextLoads() {
    }

    @Test
    public void  pepeApiEndPointNotNull(){
        assertThat(configuration.getPepeApiEndpoint(), Matchers.notNullValue());
    }

}
