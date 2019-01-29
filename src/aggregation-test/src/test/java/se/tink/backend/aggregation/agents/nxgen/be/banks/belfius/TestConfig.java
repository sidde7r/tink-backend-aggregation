package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius;

import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.agents.rpc.Credentials;
import static se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants.Storage.DEVICE_TOKEN;

public class TestConfig {

    public static final String USERNAME = "";
    public static final String PASSWORD = "";
    public static final PersistentStorage PERSISTENT_STORAGE = new PersistentStorage();
    public static final Credentials CREDENTIALS = new Credentials();

    static {
        PERSISTENT_STORAGE.put(DEVICE_TOKEN, "");
        CREDENTIALS.setUsername(TestConfig.USERNAME);
        CREDENTIALS.setPassword(TestConfig.PASSWORD);
    }
}