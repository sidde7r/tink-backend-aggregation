package se.tink.agent.sdk.operation;

import java.util.Optional;
import se.tink.backend.agents.rpc.Field;

public interface StaticBankCredentials {
    /**
     * @param key The key to resolve the value.
     * @return Optional.of(value) if key exists and its value is not NullOrEmpty, otherwise
     *     Optional.empty()
     */
    Optional<String> tryGet(String key);

    default String get(String key) {
        return this.tryGet(key)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        String.format(
                                                "The static bank credentials did not contain key '%s'.",
                                                key)));
    }

    default Optional<String> tryGet(Field.Key key) {
        return this.tryGet(key.getFieldKey());
    }

    default String get(Field.Key key) {
        return this.get(key.getFieldKey());
    }
}
