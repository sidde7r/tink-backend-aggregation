package se.tink.agent.sdk.storage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@JsonDeserialize(using = SerializableReferenceDeserializer.class)
@JsonSerialize(using = SerializableReferenceSerializer.class)
public class SerializableReference implements Reference {
    private static final String STORAGE_KEY = "reference";

    final SerializableStorage storage;

    SerializableReference(SerializableStorage storage) {
        this.storage = storage;
    }

    @Override
    public Optional<String> tryGet() {
        return this.tryGet(String.class);
    }

    @Override
    public <T> Optional<T> tryGet(Class<T> referenceType) {
        return this.storage.tryGet(STORAGE_KEY, referenceType);
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
