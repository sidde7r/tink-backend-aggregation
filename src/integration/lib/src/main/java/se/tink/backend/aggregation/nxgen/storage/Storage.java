package se.tink.backend.aggregation.nxgen.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableSet;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import io.reactivex.rxjava3.subjects.Subject;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.serialization.utils.JsonFlattener;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class Storage extends HashMap<String, String> {
    private static Logger LOG = LoggerFactory.getLogger(Storage.class);

    private Subject<Collection<String>> secretValuesSubject = ReplaySubject.create();

    private Storage(Map<String, String> map) {
        super(map);
    }

    public Storage() {}

    public static Storage copyOf(Map<String, String> map) {
        return new Storage(map);
    }

    @Override
    public String put(String key, String value) {
        secretValuesSubject.onNext(ImmutableSet.of(value));
        return super.put(key, value);
    }

    public void put(String key, Object value) {
        final String valueToStore =
                value instanceof String
                        ? (String) value
                        : SerializationUtils.serializeToString(value);
        super.put(key, valueToStore);
        final Map<String, String> newSensitiveValues;
        try {
            newSensitiveValues = JsonFlattener.flattenJsonToMap(valueToStore);
            secretValuesSubject.onNext(ImmutableSet.copyOf(newSensitiveValues.values()));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to extract sensitive information from new value to be stored.", e);
        }
    }

    public <T> Optional<T> get(String key, TypeReference<T> valueType) {
        T data =
                !containsKey(key)
                        ? null
                        : SerializationUtils.deserializeFromString(get(key), valueType);

        return Optional.ofNullable(data);
    }

    public <T> Optional<T> get(String key, Class<T> valueType) {
        if (valueType == String.class) {
            return Optional.ofNullable((T) get(key));
        }
        T data =
                !containsKey(key)
                        ? null
                        : SerializationUtils.deserializeFromString(get(key), valueType);

        return Optional.ofNullable(data);
    }

    public Subject<Collection<String>> getSecretValuesSubject() {
        return secretValuesSubject;
    }
}
