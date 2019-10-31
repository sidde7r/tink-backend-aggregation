package se.tink.backend.aggregation.nxgen.storage;

import com.google.common.collect.ImmutableSet;
import io.reactivex.rxjava3.core.Observable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import se.tink.libraries.serialization.utils.JsonFlattener;

public interface SensitiveValuesStorage {
    Observable<Collection<String>> getSensitiveValuesObservable();

    static Set<String> extractSensitiveValues(String valueToStore) {
        final Map<String, String> newSensitiveValuesMap;
        try {
            newSensitiveValuesMap = JsonFlattener.flattenJsonToMap(valueToStore);
            Set<String> newSensitiveValues = new HashSet<>(newSensitiveValuesMap.values());
            newSensitiveValues.remove(null);
            return ImmutableSet.copyOf(newSensitiveValues);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to extract sensitive information from new value to be stored.", e);
        }
    }
}
