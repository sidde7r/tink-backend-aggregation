package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class HandelsbankenSETestConfig {

    public static final String USER_NAME = "";
    public static final String HEDBERG = "";
    public static final String HEDBERG_ACCOUNT = "";
    public static final String UNKNOWN_ACCOUNT = "";
    public static final String KNOWN_ACCOUNT = "";
    public static final String PASSWORD = "";
    public static final PersistentStorage PERSISTENT_STORAGE = new PersistentStorage();

    static {
        PERSISTENT_STORAGE.put(HandelsbankenConstants.Storage.PRIVATE_KEY,
                "");
        PERSISTENT_STORAGE.put(HandelsbankenConstants.Storage.PROFILE_ID, "");
        PERSISTENT_STORAGE.put(HandelsbankenConstants.Storage.DEVICE_SECURITY_CONTEXT_ID, "");
    }
}
