package se.tink.backend.aggregation.nxgen.http_api_client.variable_detection.storage;

import se.tink.libraries.aggregation_agent_api_client.src.variable.VariableStore;

public interface VariableDetector {

    boolean detectVariableFromInsertion(
            VariableStore variableStore, String storageKey, Object storageValue);

    boolean detectVariableFromStorage(
            VariableStore variableStore, String storageKey, String storageValue);
}
