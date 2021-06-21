package se.tink.backend.aggregation.nxgen.http_api_client.variable_detection.storage;

import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.libraries.aggregation_agent_api_client.src.variable.VariableKey;
import se.tink.libraries.aggregation_agent_api_client.src.variable.VariableStore;

public class ClientIdDetector implements VariableDetector {
    private static final List<String> POSSIBLE_STORAGE_KEYS =
            ImmutableList.<String>builder()
                    .add("clientid")
                    .add("client_id")
                    .add("client-id")
                    .build();

    @Override
    public boolean detectVariableFromInsertion(
            VariableStore variableStore, String storageKey, Object storageValue) {
        if (!(VariableDetectorUtils.isInList(storageKey, POSSIBLE_STORAGE_KEYS)
                && storageValue instanceof String)) {
            return false;
        }

        variableStore.addVariable(VariableKey.CLIENT_ID, storageValue);
        return true;
    }

    @Override
    public boolean detectVariableFromStorage(
            VariableStore variableStore, String storageKey, String storageValue) {
        if (!VariableDetectorUtils.isInList(storageKey, POSSIBLE_STORAGE_KEYS)) {
            return false;
        }

        variableStore.addVariable(VariableKey.CLIENT_ID, storageValue);
        return true;
    }
}
