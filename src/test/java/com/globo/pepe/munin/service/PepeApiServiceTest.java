package com.globo.pepe.munin.service;

import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PepeApiServiceTest  {

    @Autowired
    private PepeApiService pepeApiService;

    @Test
    public void contextLoads() {
    }

    @Test
    public void  getRestTemplate(){
       RestTemplate restTemplate =  pepeApiService.getRestTemplate();
        assertThat(restTemplate, Matchers.notNullValue());
    }

}
