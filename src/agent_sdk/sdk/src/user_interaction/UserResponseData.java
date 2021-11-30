package se.tink.agent.sdk.user_interaction;

import java.util.Optional;

public interface UserResponseData {

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
                                                "The user response data did not contain key '%s'.",
                                                key)));
    }
}
