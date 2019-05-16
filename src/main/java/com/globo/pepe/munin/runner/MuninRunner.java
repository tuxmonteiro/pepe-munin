package com.globo.pepe.munin.runner;

import com.globo.pepe.common.services.JsonLoggerService;
import com.globo.pepe.munin.service.MuninService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class MuninRunner {

    private final MuninService muninService;
    private final JsonLoggerService jsonLoggerService;

    public MuninRunner(MuninService muninService, JsonLoggerService jsonLoggerService) {
        this.muninService = muninService;
        this.jsonLoggerService = jsonLoggerService;
    }

    @Scheduled(fixedDelayString = "${pepe.munin.fixedDelay}")
    public void run() {
        try {
            muninService.send();
        } catch (Exception e){
            jsonLoggerService.newLogger(getClass()).message(e.getMessage()).sendError();
        }
    }
}
