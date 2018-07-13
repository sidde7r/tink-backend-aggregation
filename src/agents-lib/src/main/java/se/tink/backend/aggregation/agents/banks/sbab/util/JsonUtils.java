package se.tink.backend.aggregation.agents.banks.sbab.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static boolean isValidJson(String input) {
        try {
            mapper.readTree(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
