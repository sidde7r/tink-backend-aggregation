package se.tink.agent.sdk.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import se.tink.agent.sdk.models.authentication.RefreshableAccessToken;
import se.tink.libraries.serialization.utils.SerializationUtils;

@EqualsAndHashCode
@JsonSerialize(using = SerializableStorageSerializer.class)
@JsonDeserialize(using = SerializableStorageDeserializer.class)
public class SerializableStorage implements Storage {
    final HashMap<String, String> storage;

    SerializableStorage(HashMap<String, String> storage) {
        this.storage = storage;
    }

    public SerializableStorage() {
        this(new HashMap<>());
    }

    @Override
    public String put(String key, Object value) {
        final String valueToStore =
                value instanceof String
                        ? (String) value
                        : SerializationUtils.serializeToString(value);

        return this.storage.put(key, valueToStore);
    }

    @Override
    public String get(String key) {
        return this.storage.getOrDefault(key, null);
    }

    @Override
    public Optional<String> tryGet(String key) {
        return Optional.ofNullable(this.get(key));
    }

    @Override
    public <T> T get(String key, Class<T> valueType) {
        final String value = this.get(key);
        if (Objects.isNull(value)) {
            return null;
        }

        if (String.class.isAssignableFrom(valueType)) {
            return (T) value;
        }

        return SerializationUtils.deserializeFromString(value, valueType);
    }

    @Override
    public <T> Optional<T> tryGet(String key, Class<T> valueType) {
        return Optional.ofNullable(this.get(key, valueType));
    }

    @Override
    public <T> T get(String key, TypeReference<T> valueType) {
        final String value = this.get(key);
        if (Objects.isNull(value)) {
            return null;
        }

        return SerializationUtils.deserializeFromString(value, valueType);
    }

    @Override
    public <T> Optional<T> tryGet(String key, TypeReference<T> valueType) {
        return Optional.ofNullable(this.get(key, valueType));
    }

    @Override
    public void putAccessToken(RefreshableAccessToken token) {
        this.put(RefreshableAccessToken.STORAGE_KEY, token);
    }

    @Override
    public Optional<RefreshableAccessToken> getAccessToken() {
        return this.tryGet(RefreshableAccessToken.STORAGE_KEY, RefreshableAccessToken.class);
    }

    @Override
    public Optional<RefreshableAccessToken> getAccessToken(String alternativeKey) {
        return Stream.of(
                        getAccessToken(), this.tryGet(alternativeKey, RefreshableAccessToken.class))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public String serialize() {
        return SerializationUtils.serializeToString(this.storage);
    }

    public static SerializableStorage deserialize(String data) {
        HashMap<String, String> map =
                SerializationUtils.deserializeFromString(
                        data, new TypeReference<HashMap<String, String>>() {});
        return new SerializableStorage(map);
    }
}
