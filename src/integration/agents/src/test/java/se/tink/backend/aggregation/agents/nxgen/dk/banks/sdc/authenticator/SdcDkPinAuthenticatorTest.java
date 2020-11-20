package se.tink.backend.aggregation.agents.nxgen.dk.banks.sdc.authenticator;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.sdcdk.SdcDkConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcPinAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.filter.SdcExceptionFilter;
import se.tink.backend.aggregation.nxgen.http.LegacyTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n.Catalog;

public class SdcDkPinAuthenticatorTest {

    @Test
    public void canAuthenticate() throws Exception {
        String username = "";
        String password = "";
        String providerId = "9695";

        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setUsername(username);
        credentials.setPassword(password);
        Provider provider = new Provider();
        provider.setPayload(providerId);
        SdcDkConfiguration configuration = new SdcDkConfiguration(provider);
        SdcSessionStorage sessionStorage = new SdcSessionStorage(new SessionStorage());
        SdcPersistentStorage persistentStorage = new SdcPersistentStorage(new PersistentStorage());

        TinkHttpClient tinkHttpClient = new LegacyTinkHttpClient();
        tinkHttpClient.addFilter(new SdcExceptionFilter());
        SdcApiClient apiClient =
                new SdcApiClient(tinkHttpClient, configuration, mock(Catalog.class));

        new SdcPinAuthenticator(apiClient, sessionStorage, configuration)
                .authenticate(username, password);

        assertNotNull(sessionStorage.getAgreements());
    }
}
