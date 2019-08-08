package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken;

import static org.mockito.Mockito.spy;

import org.junit.Before;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.CrossKeyAutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public abstract class AlandsBankenTest {

    protected CrossKeyApiClient client;
    protected Credentials credentials;

    @Before
    public void setUp() throws Exception {
        credentials = new Credentials();
        credentials.setField(Field.Key.USERNAME, AlandsBankenTestConfig.USERNAME);
        credentials.setField(Field.Key.PASSWORD, AlandsBankenTestConfig.PASSWORD);
        credentials.setType(CredentialsTypes.PASSWORD);
        PersistentStorage persistentStorage = new PersistentStorage();
        persistentStorage.put(
                CrossKeyConstants.Storage.DEVICE_ID, AlandsBankenTestConfig.DEVICE_ID);
        persistentStorage.put(
                CrossKeyConstants.Storage.DEVICE_TOKEN, AlandsBankenTestConfig.DEVICE_TOKEN);
        client =
                spy(new CrossKeyApiClient(new TinkHttpClient(), new AlandsBankenFIConfiguration()));
        CrossKeyAutoAuthenticator authenticator =
                new CrossKeyAutoAuthenticator(
                        client, new CrossKeyPersistentStorage(persistentStorage));
        authenticator.autoAuthenticate(credentials);
    }
}
