package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken;

import org.junit.Before;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.AlandsBankenAutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import static org.mockito.Mockito.spy;

public abstract class AlandsBankenTest {

    protected AlandsBankenApiClient client;
    protected Credentials credentials;

    @Before
    public void setUp() throws Exception {
        credentials = new Credentials();
        credentials.setField(Field.Key.USERNAME, AlandsBankenTestConfig.USERNAME);
        credentials.setField(Field.Key.PASSWORD, AlandsBankenTestConfig.PASSWORD);
        credentials.setType(CredentialsTypes.PASSWORD);
        PersistentStorage persistentStorage = new PersistentStorage();
        persistentStorage.put(AlandsBankenConstants.Storage.DEVICE_ID, AlandsBankenTestConfig.DEVICE_ID);
        persistentStorage.put(AlandsBankenConstants.Storage.DEVICE_TOKEN, AlandsBankenTestConfig.DEVICE_TOKEN);
        client = spy(new AlandsBankenApiClient(new TinkHttpClient(null, credentials)));
        AlandsBankenAutoAuthenticator authenticator = new AlandsBankenAutoAuthenticator(client,
                new AlandsBankenPersistentStorage(persistentStorage), credentials);
        authenticator.autoAuthenticate();
    }
}
