package com.globo.pepe.munin.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.globo.pepe.munin.util.JsonNodeUtil;
import org.springframework.stereotype.Service;
@Service
public class SofiaRepository {


    public JsonNode findByMetrics(){
        return JsonNodeUtil.buildJsonNode();
    }

}
