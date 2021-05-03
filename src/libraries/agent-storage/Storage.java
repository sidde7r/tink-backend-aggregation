package se.tink.backend.aggregation.nxgen.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class Storage extends HashMap<String, String> {

    private Storage(Map<String, String> map) {
        super(map);
    }

    public Storage() {}

    public static Storage copyOf(Map<String, String> map) {
        return new Storage(map);
    }

    public String put(String key, Object value) {
        final String valueToStore =
                value instanceof String
                        ? (String) value
                        : SerializationUtils.serializeToString(value);
        super.put(key, valueToStore);
        return valueToStore;
    }

    public <T> Optional<T> get(String key, TypeReference<T> valueType) {
        T data =
                !containsKey(key)
                        ? null
                        : SerializationUtils.deserializeFromString(get(key), valueType);

        return Optional.ofNullable(data);
    }

    public <T> Optional<T> get(String key, Class<T> valueType) {
        if (valueType == String.class) {
            return Optional.ofNullable((T) get(key));
        }
        T data =
                !containsKey(key)
                        ? null
                        : SerializationUtils.deserializeFromString(get(key), valueType);

        return Optional.ofNullable(data);
    }
}
