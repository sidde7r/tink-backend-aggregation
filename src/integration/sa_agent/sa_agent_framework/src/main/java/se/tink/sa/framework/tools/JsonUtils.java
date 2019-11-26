package se.tink.sa.framework.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URL;

public class JsonUtils {

    private static ObjectMapper buildDefaultObjectMapper() {
        return new ObjectMapper();
    }

    public static <Q> Q fromMessage(byte[] message, Class<Q> contentClass) throws IOException {
        return fromMessage(new String(message), contentClass);
    }

    public static <Q> Q fromMessage(String requestMessage, Class<Q> contentClass)
            throws IOException {
        ObjectMapper objectMapper = buildDefaultObjectMapper();
        Q object = objectMapper.readValue(requestMessage, contentClass);
        return object;
    }

    public static String writeAsJson(Object response) {
        ObjectMapper objectMapper = buildDefaultObjectMapper();
        String jsonObject = null;
        try {
            jsonObject = objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
        return jsonObject;
    }

    public static <T> T readJson(URL resource, Class<T> clazz) {
        ObjectMapper objectMapper = buildDefaultObjectMapper();
        T result = null;
        try {
            result = objectMapper.readValue(resource, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
