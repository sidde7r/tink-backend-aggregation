package se.tink.backend.aggregation.nxgen.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.Optional;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class Storage extends HashMap<String, String> {
    public void put(String key, Object value) {
        super.put(key, value instanceof String ? (String) value : SerializationUtils.serializeToString(value));
    }

    public <T> Optional<T> get(String key, TypeReference<T> valueType) {
        T data = !containsKey(key) ? null : SerializationUtils.deserializeFromString(get(key), valueType);

        return Optional.ofNullable(data);
    }

    public <T> Optional<T> get(String key, Class<T> valueType) {
        if (valueType == String.class) {
            return Optional.ofNullable((T) get(key));
        }
        T data = !containsKey(key) ? null : SerializationUtils.deserializeFromString(get(key), valueType);

        return Optional.ofNullable(data);
    }
}
