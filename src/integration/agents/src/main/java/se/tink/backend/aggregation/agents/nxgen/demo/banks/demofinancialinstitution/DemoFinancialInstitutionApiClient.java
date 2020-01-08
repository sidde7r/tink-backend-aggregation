package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.DemoFinancialInstitutionConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.DemoFinancialInstitutionConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.DemoFinancialInstitutionConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.configuration.DemoFinancialInstitutionConfiguration;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.transactionalaccount.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class DemoFinancialInstitutionApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private DemoFinancialInstitutionConfiguration configuration;

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DemoFinancialInstitutionApiClient.class);

    public DemoFinancialInstitutionApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public DemoFinancialInstitutionConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(DemoFinancialInstitutionConfiguration configuration) {
        this.configuration = configuration;
    }

    private URL createBaseUrl() {
        return new URL(configuration.getBaseUrl());
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final String username = sessionStorage.get(Storage.BASIC_AUTH_USERNAME);
        final String password = sessionStorage.get(Storage.BASIC_AUTH_PASSWORD);

        return createRequest(url).addBasicAuth(username, password);
    }

    public FetchAccountsResponse fetchAccounts() {
        final URL url = createBaseUrl().concat(Urls.ACCOUNTS);

        return createRequestInSession(url).get(FetchAccountsResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(String accountNumber) {
        final URL url =
                createBaseUrl().concat(Urls.TRANSACTIONS).parameter("accountNumber", accountNumber);

        return createRequestInSession(url).get(FetchTransactionsResponse.class);
    }

    public FetchTransactionsResponse fetchTransactionsForNextUrl(URL nextUrl) {
        return createRequestInSession(nextUrl).get(FetchTransactionsResponse.class);
    }
}
