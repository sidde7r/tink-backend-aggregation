package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.DemoFinancialInstitutionConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.DemoFinancialInstitutionConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.configuration.DemoFinancialInstitutionConfiguration;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.transactionalaccount.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class DemoFinancialInstitutionApiClient {

    private final TinkHttpClient client;
    private DemoFinancialInstitutionConfiguration configuration;

    private static final Logger LOGGER =
            LoggerFactory.getLogger(DemoFinancialInstitutionApiClient.class);

    public DemoFinancialInstitutionApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public DemoFinancialInstitutionConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(DemoFinancialInstitutionConfiguration configuration) {
        this.configuration = configuration;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        final HttpResponse httpResponse =
                createRequest(createBaseUrl().concat(Urls.LOGIN))
                        .post(HttpResponse.class, loginRequest);

        LOGGER.debug("DFI_request: %s", httpResponse.getRequest().toString());
        LOGGER.debug("DFI_headers: %s", httpResponse.getHeaders().toString());
        LOGGER.debug("DFI_cookies: %s", httpResponse.getCookies().toString());

        return httpResponse.getBody(LoginResponse.class);
    }

    private URL createBaseUrl() {
        return new URL(configuration.getBaseUrl());
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    public FetchAccountsResponse fetchAccounts() {
        final URL url = createBaseUrl().concat(Urls.ACCOUNTS);

        return createRequest(url).get(FetchAccountsResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(String accountNumber) {
        final URL url =
                createBaseUrl().concat(Urls.TRANSACTIONS).parameter("accountNumber", accountNumber);

        return createRequest(url).get(FetchTransactionsResponse.class);
    }

    public FetchTransactionsResponse fetchTransactionsForNextUrl(URL nextUrl) {
        return createRequest(nextUrl).get(FetchTransactionsResponse.class);
    }
}
