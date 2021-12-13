package se.tink.agent.runtime.environment;

import java.util.HashMap;
import se.tink.agent.sdk.environment.Utilities;

public class RuntimeEnvironment {
    private final HashMap<String, String> rawStorage;
    private final Utilities utilities;

    public RuntimeEnvironment(HashMap<String, String> rawStorage, Utilities utilities) {
        this.rawStorage = rawStorage;
        this.utilities = utilities;
    }
}
