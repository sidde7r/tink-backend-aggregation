package se.tink.backend.aggregation.nxgen.storage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import se.tink.libraries.serialization.utils.JsonFlattener;

public final class StorageUtils {

    private StorageUtils() {
        throw new AssertionError();
    }

    static Set<String> extractSensitiveValues(String valueToStore) {
        final Map<String, String> newSensitiveValuesMap;
        try {
            newSensitiveValuesMap = JsonFlattener.flattenJsonToMap(valueToStore);
            Set<String> newSensitiveValues = new HashSet<>(newSensitiveValuesMap.values());
            newSensitiveValues.remove(null);
            return newSensitiveValues;
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to extract sensitive information from new value to be stored.", e);
        }
    }
}
