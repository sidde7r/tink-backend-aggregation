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
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

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
                spy(
                        new CrossKeyApiClient(
                                NextGenTinkHttpClient.builder(
                                                new FakeLogMasker(),
                                                LogMasker.LoggingMode
                                                        .UNSURE_IF_MASKER_COVERS_SECRETS)
                                        .build(),
                                new AlandsBankenFIConfiguration(),
                                new SessionStorage()));
        CrossKeyAutoAuthenticator authenticator =
                new CrossKeyAutoAuthenticator(
                        client, new CrossKeyPersistentStorage(persistentStorage), credentials);
        authenticator.autoAuthenticate();
    }
}
