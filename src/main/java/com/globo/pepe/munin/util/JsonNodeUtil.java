package com.globo.pepe.munin.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonNodeUtil {

    public static JsonNode buildJsonNode(){
        JsonNode node = ((ObjectNode) JsonNodeFactory.instance.objectNode());
        return node;
    }

}
