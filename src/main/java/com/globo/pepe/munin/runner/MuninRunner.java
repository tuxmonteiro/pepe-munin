package com.globo.pepe.munin.runner;

import com.globo.pepe.munin.service.MuninService;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class MuninRunner {

    @Autowired
    private MuninService muninService;

    @Scheduled(fixedDelayString = "${munin.fixedDelay}")
    public void run() {
       muninService.send();
    }


}
