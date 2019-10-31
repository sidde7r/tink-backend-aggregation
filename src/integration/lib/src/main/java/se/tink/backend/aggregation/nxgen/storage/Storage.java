package se.tink.backend.aggregation.nxgen.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableSet;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import io.reactivex.rxjava3.subjects.Subject;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import se.tink.libraries.serialization.utils.JsonFlattener;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class Storage extends HashMap<String, String> {
    private Subject<Collection<String>> secretValuesSubject =
            ReplaySubject.<Collection<String>>create().toSerialized();

    private Storage(Map<String, String> map) {
        super(map);
    }

    public Storage() {}

    public static Storage copyOf(Map<String, String> map) {
        return new Storage(map);
    }

    @Override
    public String put(String key, String value) {
        Optional.ofNullable(value).ifPresent(v -> secretValuesSubject.onNext(ImmutableSet.of(v)));
        return super.put(key, value);
    }

    public void put(String key, Object value) {
        final String valueToStore =
                value instanceof String
                        ? (String) value
                        : SerializationUtils.serializeToString(value);
        super.put(key, valueToStore);
        final Map<String, String> newSensitiveValuesMap;
        try {
            newSensitiveValuesMap = JsonFlattener.flattenJsonToMap(valueToStore);
            Set<String> newSensitiveValues = new HashSet<>(newSensitiveValuesMap.values());
            newSensitiveValues.remove(null);
            secretValuesSubject.onNext(ImmutableSet.copyOf(newSensitiveValues));
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

    public Observable<Collection<String>> getSecretValuesObservable() {
        return secretValuesSubject;
    }
}
