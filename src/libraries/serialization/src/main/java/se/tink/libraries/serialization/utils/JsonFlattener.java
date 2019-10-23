package se.tink.libraries.serialization.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonFlattener {
    public static final String ROOT_PATH = "";
    private static Logger LOG = LoggerFactory.getLogger(JsonFlattener.class);

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static Map<String, String> flattenJsonToMap(String currentPath, JsonNode jsonNode)
            throws IOException {
        Map<String, String> map = new HashMap<>(Collections.emptyMap());
        if (jsonNode.isObject()) {
            ObjectNode objectNode = (ObjectNode) jsonNode;
            Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();
            String pathPrefix = currentPath.isEmpty() ? "" : currentPath + ".";

            while (iter.hasNext()) {
                Map.Entry<String, JsonNode> entry = iter.next();
                map.putAll(flattenJsonToMap(pathPrefix + entry.getKey(), entry.getValue()));
            }
        } else if (jsonNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) jsonNode;
            for (int i = 0; i < arrayNode.size(); i++) {
                map.putAll(flattenJsonToMap(currentPath + "[" + i + "]", arrayNode.get(i)));
            }
        } else if (jsonNode.isValueNode()) {
            // This is empirically tested. When we have some nested json objects serialized as a
            // String, depending on how many levels the object has the unescaping of characters
            // might make it not work. These conditions checking the lenght of the escaped/unescaped
            // again serialized String make it work.
            ValueNode valueNode = (ValueNode) jsonNode;
            String unescaped = StringEscapeUtils.unescapeJson(valueNode.asText());
            int lengthUnescaped = unescaped.length();
            int lengthEscaped = valueNode.asText().length();
            int lengthEscapedAgain = StringEscapeUtils.escapeJson(unescaped).length();
            if (lengthEscaped == lengthUnescaped && lengthEscaped == lengthEscapedAgain) {
                map.put(currentPath, valueNode.asText());
            } else {
                final String treeString;
                if (lengthUnescaped < lengthEscaped && lengthEscaped < lengthEscapedAgain) {
                    treeString = valueNode.asText();
                } else {
                    treeString = unescaped;
                }
                JsonNode tryIfJsonNodeIsJsonObject = OBJECT_MAPPER.readTree(treeString);
                map.putAll(flattenJsonToMap(currentPath, tryIfJsonNodeIsJsonObject));
            }
        }
        return map;
    }
}
