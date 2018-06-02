package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken;

import org.junit.Before;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.HandelsbankenAutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;

public abstract class HandelsbankenSEAuthenticatedTest {

    protected HandelsbankenAutoAuthenticator autoAuthenticator;
    protected HandelsbankenPersistentStorage persistentStorage;
    protected HandelsbankenSessionStorage sessionStorage;
    protected HandelsbankenSEApiClient client;
    protected Credentials credentials;

    @Before
    public void setUp() throws Exception {
        credentials = new Credentials();
        credentials.setField(Field.Key.USERNAME, HandelsbankenSETestConfig.USER_NAME);
        credentials.setField(Field.Key.PASSWORD, HandelsbankenSETestConfig.PASSWORD);
        credentials.setType(CredentialsTypes.PASSWORD);
        persistentStorage = new HandelsbankenPersistentStorage(
                HandelsbankenSETestConfig.PERSISTENT_STORAGE);
        HandelsbankenSEConfiguration handelsbankenConfiguration = new HandelsbankenSEConfiguration();
        client = new HandelsbankenSEApiClient(new TinkHttpClient
                (null, credentials), handelsbankenConfiguration);
        sessionStorage = new HandelsbankenSessionStorage(new SessionStorage(), handelsbankenConfiguration);

        autoAuthenticator = new HandelsbankenAutoAuthenticator(client, persistentStorage, credentials, sessionStorage,
                handelsbankenConfiguration);

    }
}
