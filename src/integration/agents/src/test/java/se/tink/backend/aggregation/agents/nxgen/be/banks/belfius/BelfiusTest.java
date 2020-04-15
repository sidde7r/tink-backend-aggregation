package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.AuthenticatorSleepHelper;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.signature.BelfiusSignatureCreator;
import se.tink.backend.aggregation.configuration.ProviderConfig;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationControllerImpl;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelperImpl;
import se.tink.backend.aggregation.nxgen.http.LegacyTinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BelfiusTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    protected final SupplementalInformationController supplementalInformation =
            mock(SupplementalInformationControllerImpl.class);
    protected BelfiusApiClient apiClient;
    protected BelfiusSessionStorage sessionStorage;

    protected BelfiusAuthenticator setupAuthentication(
            PersistentStorage persistentStorage, Credentials credentials) {

        this.apiClient =
                spy(
                        new BelfiusApiClient(
                                new LegacyTinkHttpClient(),
                                new BelfiusSessionStorage(new SessionStorage()),
                                BelfiusConstants.Request.LOCALE_DUTCH));
        ProviderConfig marketProviders = readProvidersConfiguration("be");
        Provider provider = marketProviders.getProvider("be-belfius-cardreader");
        provider.setMarket(marketProviders.getMarket());
        provider.setCurrency(marketProviders.getCurrency());
        return new BelfiusAuthenticator(
                this.apiClient,
                credentials,
                persistentStorage,
                sessionStorage,
                new SupplementalInformationHelperImpl(provider, supplementalInformation),
                new BelfiusSignatureCreator(),
                new AuthenticatorSleepHelper());
    }

    // TODO Move this out to test helper.
    private String escapeMarket(String market) {
        return market.replaceAll("[^a-zA-Z]", "");
    }

    private ProviderConfig readProvidersConfiguration(String market) {
        String providersFilePath =
                "external/tink_backend/src/provider_configuration/data/seeding/providers-"
                        + escapeMarket(market).toLowerCase()
                        + ".json";
        File providersFile = new File(providersFilePath);
        try {
            return mapper.readValue(providersFile, ProviderConfig.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    protected void autoAuthenticate()
            throws SessionException, LoginException, AuthorizationException {
        BelfiusAuthenticator authenticator =
                setupAuthentication(TestConfig.PERSISTENT_STORAGE, TestConfig.CREDENTIALS);

        authenticator.autoAuthenticate();
    }
}
