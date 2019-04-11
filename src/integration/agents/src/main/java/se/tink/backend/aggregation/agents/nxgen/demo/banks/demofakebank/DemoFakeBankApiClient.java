package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.DemoFakeBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.authenticator.DemoFakeBankAuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.authenticator.rpc.DemoFakeBankAuthenticationBody;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.configuration.DemoFakeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.fetcher.transactionalaccount.rpc.DemoFakeBankAccountBody;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.fetcher.transactionalaccount.rpc.DemoFakeBankAccountsResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class DemoFakeBankApiClient {
    private final TinkHttpClient client;
    private PersistentStorage persistentStorage;
    private DemoFakeBankConfiguration configuration;

    public DemoFakeBankApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    public DemoFakeBankConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(DemoFakeBankConfiguration configuration) {
        this.configuration = configuration;
    }

    public DemoFakeBankAuthenticateResponse authenticate(
            DemoFakeBankAuthenticationBody authenticationBody) {
        return createRequest(createBaseUrl().concat(DemoFakeBankConstants.Urls.AUTHENTICATE))
                .post(DemoFakeBankAuthenticateResponse.class, authenticationBody);
    }

    private URL createBaseUrl() {
        return new URL(persistentStorage.get(DemoFakeBankConstants.Storage.BASE_URL));
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    public DemoFakeBankAccountsResponse fetchAccounts(String username, String token) {
        return createRequest(createBaseUrl().concat(DemoFakeBankConstants.Urls.ACCOUNTS))
                .post(
                        DemoFakeBankAccountsResponse.class,
                        new DemoFakeBankAccountBody(username, token));
    }
}
