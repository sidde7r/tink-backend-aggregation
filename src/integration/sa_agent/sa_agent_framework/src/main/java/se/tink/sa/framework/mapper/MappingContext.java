package se.tink.sa.framework.mapper;

import java.util.HashMap;
import java.util.Map;

public class MappingContext {

    private Map<String, Object> ctxMap;

    public static MappingContext newInstance() {
        return new MappingContext();
    }

    public MappingContext() {
        this.ctxMap = new HashMap<>();
    }

    public <T> MappingContext put(String key, T value) {
        ctxMap.put(key, value);
        return this;
    }

    public <T> T get(String key) {
        return (T) ctxMap.get(key);
    }
}
