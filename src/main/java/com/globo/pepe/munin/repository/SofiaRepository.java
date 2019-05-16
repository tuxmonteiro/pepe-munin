package com.globo.pepe.munin.repository;

import com.globo.pepe.common.services.JsonLoggerService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
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
        List<Map<String, Object>> result = null;
        try {
            result = jdbcTemplate.queryForList(query);

        } catch (Exception e) {
            jsonLoggerService.newLogger(getClass()).put("short_message", e.getMessage()).sendError();
        }
        return result;
    }

}
