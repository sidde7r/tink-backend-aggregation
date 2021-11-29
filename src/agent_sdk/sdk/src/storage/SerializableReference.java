package se.tink.agent.sdk.storage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@JsonDeserialize(using = SerializableReferenceDeserializer.class)
@JsonSerialize(using = SerializableReferenceSerializer.class)
public class SerializableReference {
    private static final String STORAGE_KEY = "reference";

    final SerializableStorage storage;

    SerializableReference(SerializableStorage storage) {
        this.storage = storage;
    }

    public <T> T get(Class<T> referenceType) {
        return this.storage
                .tryGet(STORAGE_KEY, referenceType)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "The Reference could not be found or failed to be deserialized."));
    }

    public String get() {
        return this.get(String.class);
    }

    public static SerializableReference from(String reference) {
        return from((Object) reference);
    }

    public static SerializableReference from(Object reference) {
        SerializableStorage storage = new SerializableStorage();
        storage.put(STORAGE_KEY, reference);
        return new SerializableReference(storage);
    }
}
