package se.tink.backend.aggregation.utils.json;

import com.fasterxml.jackson.core.JsonProcessingException;
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

    public static String prettyJson(Object json) {
        try {
            return mapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(json);
        } catch (JsonProcessingException je) {
            throw new IllegalStateException(je);
        }
    }

    public static void prettyPrintJson(Object json) {
        System.out.println(prettyJson(json));
    }
}
