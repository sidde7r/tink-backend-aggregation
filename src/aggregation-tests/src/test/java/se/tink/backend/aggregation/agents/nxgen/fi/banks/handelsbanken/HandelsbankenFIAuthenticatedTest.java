package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken;

import java.util.Collections;
import org.junit.Before;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.HandelsbankenAutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;

public abstract class HandelsbankenFIAuthenticatedTest {
    protected HandelsbankenAutoAuthenticator autoAuthenticator;
    protected HandelsbankenPersistentStorage persistentStorage;
    protected HandelsbankenSessionStorage sessionStorage;
    protected HandelsbankenFIApiClient client;
    protected Credentials credentials;

    @Before
    public void setUp() throws Exception {
        HandelsbankenFITestConfig config = getTestConfig();

        credentials = new Credentials();
        credentials.setField(Field.Key.USERNAME, config.getUserName());
        credentials.setField(Field.Key.PASSWORD, config.getPassword());
        credentials.setType(CredentialsTypes.PASSWORD);
        persistentStorage = new HandelsbankenPersistentStorage(config.getPersistentStorage(), Collections.emptyMap());
        HandelsbankenFIConfiguration handelsbankenConfiguration = new HandelsbankenFIConfiguration();
        this.client = new HandelsbankenFIApiClient(new TinkHttpClient
                (null, credentials), handelsbankenConfiguration);
        sessionStorage = new HandelsbankenSessionStorage(new SessionStorage(),
                handelsbankenConfiguration);

        autoAuthenticator = new HandelsbankenAutoAuthenticator(this.client, persistentStorage, credentials, sessionStorage,
                handelsbankenConfiguration);

    }

    protected abstract HandelsbankenFITestConfig getTestConfig();
}
