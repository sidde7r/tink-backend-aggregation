package se.tink.agent.sdk.storage;

import java.util.Optional;

public interface Reference {

    Optional<String> tryGet();

    <T> Optional<T> tryGet(Class<T> referenceType);

    default String get() {
        return this.get(String.class);
    }

    default <T> T get(Class<T> referenceType) {
        return this.tryGet(referenceType)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "The Reference could not be found or failed to be deserialized."));
    }
}
