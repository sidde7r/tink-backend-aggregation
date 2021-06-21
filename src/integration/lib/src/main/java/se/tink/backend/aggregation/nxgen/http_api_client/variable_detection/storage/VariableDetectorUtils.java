package se.tink.backend.aggregation.nxgen.http_api_client.variable_detection.storage;

import java.util.List;

public class VariableDetectorUtils {

    private VariableDetectorUtils() {
        throw new IllegalStateException("Utility class");
    }

    static boolean isInList(String storageKey, List<String> keys) {
        String lowerCaseStorageKey = storageKey.toLowerCase();
        return keys.stream().anyMatch(lowerCaseStorageKey::contains);
    }
}
