package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserDataDeserializer extends JsonDeserializer<Map<String, String>> {

    @Override
    public Map<String, String> deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        ObjectCodec oc = p.getCodec();
        JsonNode node = oc.readTree(p);

        node = node.get("pair"); // Skip one level of JSON tree.

        int numElements = node.size();
        Map<String, String> userData = new HashMap<>(numElements);

        // Extract key/value pairs and populate map.
        for(int i = 0; i < numElements; i++){

            JsonNode dataElement = node.get(i);
            userData.put(dataElement.get("key").asText(), dataElement.get("value").asText());
        }

        return userData;
    }
}
