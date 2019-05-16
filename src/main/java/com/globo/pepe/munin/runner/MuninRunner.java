package com.globo.pepe.munin.runner;

import com.globo.pepe.munin.service.MuninService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class MuninRunner {

    private final MuninService muninService;

    @Scheduled(fixedDelayString = "${pepe.munin.fixedDelay}")
    public void run() {
       muninService.send();
    }

    public MuninRunner(MuninService muninService) {
        this.muninService = muninService;
    }
}
