package se.tink.backend.aggregation.agents.framework.wiremock.errordetector;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class BodyEntity {

    private static ObjectMapper mapper = new ObjectMapper();

    public enum BodyType {
        EMPTY,
        MAP,
        LIST,
        TEXT
    }

    private BodyType bodyType;
    private String bodyDataSerialized;

    public BodyEntity(BodyType bodyType, String bodyDataSerialized) {
        this.bodyType = bodyType;
        this.bodyDataSerialized = bodyDataSerialized;
    }

    public BodyType getBodyType() {
        return bodyType;
    }

    public String getBodyDataSerialized() {
        return bodyDataSerialized;
    }

    public static Map<String, Object> getMap(BodyEntity entity) throws IOException {
        if (!entity.getBodyType().equals(BodyType.MAP)) {
            throw new IllegalStateException("This entity does not have a map data");
        }

        return mapper.readValue(entity.getBodyDataSerialized(), Map.class);
    }

    public static List<?> getList(BodyEntity entity) throws IOException {
        if (!entity.getBodyType().equals(BodyType.LIST)) {
            throw new IllegalStateException("This entity does not have a list data");
        }

        return mapper.readValue(entity.getBodyDataSerialized(), List.class);
    }

    public static String getText(BodyEntity entity) {
        if (!entity.getBodyType().equals(BodyType.TEXT)) {
            throw new IllegalStateException("This entity does not have a list data");
        }

        return entity.getBodyDataSerialized();
    }

    public static BodyEntity emptyBody() {
        return new BodyEntity(BodyType.EMPTY, null);
    }
}
