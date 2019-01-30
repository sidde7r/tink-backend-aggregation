package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import static se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Headers.USER_AGENT;
import static se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Storage.DEVICE_ID;
import static se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Storage.ING_ID;
import static se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Storage.OTP_KEY_HEX;
import static se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Storage.PSN;
import static se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Storage.SECRET_0_IN_HEX;
import static se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Storage.SECRET_1_IN_HEX;
import static se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Storage.SESSION_KEY_AUTH_IN_HEX;
import static se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Storage.SESSION_KEY_IN_HEX;
import static se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Storage.SYSTEM_PIN;

public class IngTestConfig {
    private static final String USERNAME = "";
    private static final String CARD_ID = "";

    private Credentials credentials;
    private IngApiClient apiClient;
    private IngHelper ingHelper;
    private PersistentStorage persistentStorage;

    public static IngTestConfig createForTwoFactorAuthentication() {
        return new IngTestConfig(false);
    }

    public static IngTestConfig createForAutoAuthentication() {
        return new IngTestConfig(true);
    }

    private IngTestConfig(boolean populatePersistentStorage) {
        this.credentials = createTestUserCredentials();
        this.apiClient = createTestApiClient();
        this.ingHelper = new IngHelper(new SessionStorage());
        this.persistentStorage = new PersistentStorage();

        if (populatePersistentStorage) {
            populatePersistentStorage();
        }
    }

    private Credentials createTestUserCredentials() {
        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setField(Field.Key.USERNAME, USERNAME);
        credentials.setField("cardId", CARD_ID);
        return credentials;
    }

    private IngApiClient createTestApiClient() {
        TinkHttpClient httpClient = new TinkHttpClient(null);
        httpClient.setFollowRedirects(false);
        httpClient.setUserAgent(USER_AGENT);
        return new IngApiClient(httpClient);
    }

    private void populatePersistentStorage() {
        this.persistentStorage.put(ING_ID, "");
        this.persistentStorage.put(DEVICE_ID, "");
        this.persistentStorage.put(PSN, "");
        this.persistentStorage.put(OTP_KEY_HEX, "");
        this.persistentStorage.put(SYSTEM_PIN, "");
        this.persistentStorage.put(SECRET_0_IN_HEX, "");
        this.persistentStorage.put(SECRET_1_IN_HEX, "");
        this.persistentStorage.put(SESSION_KEY_IN_HEX, "");
        this.persistentStorage.put(SESSION_KEY_AUTH_IN_HEX, "");
    }

    public Credentials getTestUserCredentials() {
        return credentials;
    }

    public IngApiClient getTestApiClient() {
        return apiClient;
    }

    public PersistentStorage getTestPersistentStorage() {
        return persistentStorage;
    }

    public IngHelper getTestIngHelper() {
        return ingHelper;
    }
}
