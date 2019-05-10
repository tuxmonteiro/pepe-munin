package com.globo.pepe.munin.repository;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SofiaRepository {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public  List<Map<String, Object>> findByMetrics() {
        try {
            String query = "select * from entity limit 10";
            List<Map<String, Object>> result = jdbcTemplate.queryForList(query);
            return result;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

}
