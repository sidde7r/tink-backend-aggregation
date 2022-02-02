package src.agent_sdk.runtime.src.environment;

import java.util.Map;
import se.tink.agent.sdk.environment.Utilities;

public class RuntimeEnvironment {
    private final Map<String, String> rawStorage;
    private final Utilities utilities;

    public RuntimeEnvironment(Map<String, String> rawStorage, Utilities utilities) {
        this.rawStorage = rawStorage;
        this.utilities = utilities;
    }

    public Map<String, String> getRawStorage() {
        return rawStorage;
    }

    public Utilities getUtilities() {
        return utilities;
    }
}
