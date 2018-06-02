package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.rpc.Credentials;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class BelfiusTest {

    protected final SupplementalInformationController supplementalInformation = mock(SupplementalInformationController
            .class);
    protected BelfiusApiClient apiClient;
    protected BelfiusSessionStorage sessionStorage;

    protected BelfiusAuthenticator setupAuthentication(PersistentStorage persistentStorage, Credentials credentials) {
        this.apiClient = spy(
                new BelfiusApiClient(new TinkHttpClient(null, credentials),
                        new BelfiusSessionStorage(new SessionStorage()))
        );

        return new BelfiusAuthenticator(
                this.apiClient, credentials, persistentStorage,
                this.supplementalInformation, sessionStorage);
    }

    protected void autoAuthenticate() throws SessionException {
        BelfiusAuthenticator authenticator = setupAuthentication(TestConfig.PERSISTENT_STORAGE, TestConfig.CREDENTIALS);

        authenticator.autoAuthenticate();
    }
}
