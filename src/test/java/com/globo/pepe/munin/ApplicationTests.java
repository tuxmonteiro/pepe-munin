package com.globo.pepe.munin;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@TestPropertySource(properties = {
    "pepe.keystone.url=default",
    "pepe.keystone.user=root",
    "pepe.keystone.password=",
    "pepe.keystone.domain=default",
    "pepe.keystone.project=pepe"
})
public class ApplicationTests {

	@Test
	public void contextLoads() {
	}

}
