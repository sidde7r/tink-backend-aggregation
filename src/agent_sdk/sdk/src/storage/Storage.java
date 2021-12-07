package se.tink.agent.sdk.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public interface Storage {
    void remove(String key);

    String put(String key, Object value);

    String get(String key);

    Optional<String> tryGet(String key);

    <T> T get(String key, Class<T> valueType);

    <T> Optional<T> tryGet(String key, Class<T> valueType);

    <T> T get(String key, TypeReference<T> valueType);

    <T> Optional<T> tryGet(String key, TypeReference<T> valueType);

    void putOauth2Token(OAuth2Token token);

    Optional<OAuth2Token> getOauth2Token();

    Optional<OAuth2Token> getOauth2Token(String alternativeKey);
}
