package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.framework.ProviderConfigModel;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.i18n.Catalog;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class BelfiusTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    protected final SupplementalInformationController supplementalInformation = mock(SupplementalInformationController
            .class);
    protected BelfiusApiClient apiClient;
    protected BelfiusSessionStorage sessionStorage;

    protected BelfiusAuthenticator setupAuthentication(PersistentStorage persistentStorage, Credentials credentials) {
        this.apiClient = spy(
                new BelfiusApiClient(new TinkHttpClient(null, credentials),
                        new BelfiusSessionStorage(new SessionStorage()))
        );
        ProviderConfigModel marketProviders = readProvidersConfiguration("be");
        Provider provider = marketProviders.getProvider("be-belfius-cardreader");
        provider.setMarket(marketProviders.getMarket());
        provider.setCurrency(marketProviders.getCurrency());
        return new BelfiusAuthenticator(
                this.apiClient,
                credentials,
                persistentStorage,
                sessionStorage,
                new SupplementalInformationHelper(provider, supplementalInformation));
    }

    // TODO Move this out to test helper.
    private String escapeMarket(String market) {
        return market.replaceAll("[^a-zA-Z]", "");
    }

    private ProviderConfigModel readProvidersConfiguration(String market) {
        String providersFilePath = "data/seeding/providers-" + escapeMarket(market).toLowerCase() + ".json";
        File providersFile = new File(providersFilePath);
        try {
            return mapper.readValue(providersFile, ProviderConfigModel.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
    
    protected void autoAuthenticate() throws SessionException {
        BelfiusAuthenticator authenticator = setupAuthentication(TestConfig.PERSISTENT_STORAGE, TestConfig.CREDENTIALS);

        authenticator.autoAuthenticate();
    }
}
