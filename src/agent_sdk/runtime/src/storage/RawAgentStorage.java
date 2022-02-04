package src.agent_sdk.runtime.src.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.libraries.serialization.utils.SerializationUtils;

@AllArgsConstructor
public class RawAgentStorage {
    private final Map<String, String> rawStorage;

    private <T> Optional<T> getValue(Field.Key key, TypeReference<T> typeReference) {
        return Optional.ofNullable(rawStorage)
                .map(storage -> storage.getOrDefault(key.getFieldKey(), null))
                .map(value -> SerializationUtils.deserializeFromString(value, typeReference));
    }

    public Optional<Map<String, String>> getPersistentStorage() {
        return getValue(Field.Key.PERSISTENT_STORAGE, new TypeReference<Map<String, String>>() {});
    }

    public Optional<Map<String, String>> getSessionStorage() {
        return getValue(Field.Key.SESSION_STORAGE, new TypeReference<Map<String, String>>() {});
    }
}
