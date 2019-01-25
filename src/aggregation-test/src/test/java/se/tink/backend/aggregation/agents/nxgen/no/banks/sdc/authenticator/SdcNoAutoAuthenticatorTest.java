package se.tink.backend.aggregation.agents.nxgen.no.banks.sdc.authenticator;

import org.junit.Test;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.SdcAutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import static org.junit.Assert.assertNotNull;

public class SdcNoAutoAuthenticatorTest {

    @Test
    public void canAuthenticate() throws Exception {
        String username = "";
        String password = "";
        String providerId = "3730";

        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setUsername(username);
        credentials.setPassword(password);
        Provider provider = new Provider();
        provider.setPayload(providerId);
        SdcNoConfiguration configuration = new SdcNoConfiguration(provider);
        SdcSessionStorage sessionStorage = new SdcSessionStorage(new SessionStorage());
        SdcPersistentStorage persistentStorage = new SdcPersistentStorage(new PersistentStorage());

        SdcApiClient apiClient = new SdcApiClient(new TinkHttpClient(null, credentials), configuration);

       new SdcAutoAuthenticator(
                apiClient,
                sessionStorage,
                configuration,
                credentials,
                persistentStorage
        ).autoAuthenticate();

        assertNotNull(sessionStorage.getAgreements());
    }

}
