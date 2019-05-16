package com.globo.pepe.munin.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public class JsonNodeUtil {

    public static JsonNode buildJsonNode(){
        return JsonNodeFactory.instance.objectNode();
    }

}
