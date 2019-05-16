package com.globo.pepe.munin.repository;

import com.globo.pepe.common.services.JsonLoggerService;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SofiaRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JsonLoggerService jsonLoggerService;

    public SofiaRepository(JsonLoggerService jsonLoggerService, JdbcTemplate jdbcTemplate) {
        this.jsonLoggerService = jsonLoggerService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public  List<Map<String, Object>> findByMetrics(String query) {
        try {
            return jdbcTemplate.queryForList(query);
        } catch (Exception e) {
            jsonLoggerService.newLogger(getClass()).message(e.getMessage()).sendError();
        }
        return Collections.emptyList();
    }

}
