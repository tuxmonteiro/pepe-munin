package com.globo.pepe.munin.util;

import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JsonNodeUtilTest  {


    @Test
    public void  buildJsonNodeTest(){
        JsonNode node = JsonNodeUtil.buildJsonNode();
        assertThat(node, Matchers.notNullValue());
    }

}
