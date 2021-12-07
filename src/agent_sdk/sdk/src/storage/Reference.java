package se.tink.agent.sdk.storage;

public interface Reference {
    String get();

    <T> T get(Class<T> referenceType);
}
