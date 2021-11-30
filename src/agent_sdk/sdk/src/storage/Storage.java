package se.tink.agent.sdk.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Optional;
import se.tink.agent.sdk.models.authentication.RefreshableAccessToken;

public interface Storage {
    String put(String key, Object value);

    String get(String key);

    Optional<String> tryGet(String key);

    <T> T get(String key, Class<T> valueType);

    <T> Optional<T> tryGet(String key, Class<T> valueType);

    <T> T get(String key, TypeReference<T> valueType);

    <T> Optional<T> tryGet(String key, TypeReference<T> valueType);

    void putAccessToken(RefreshableAccessToken token);

    Optional<RefreshableAccessToken> getAccessToken();

    Optional<RefreshableAccessToken> getAccessToken(String alternativeKey);
}
