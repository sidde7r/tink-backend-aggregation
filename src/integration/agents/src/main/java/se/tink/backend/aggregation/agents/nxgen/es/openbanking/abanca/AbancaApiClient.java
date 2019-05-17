package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.configuration.AbancaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class AbancaApiClient {

    private final TinkHttpClient client;
    private AbancaConfiguration configuration;

    public AbancaApiClient(TinkHttpClient client) {
        this.client = client;
    }

    private AbancaConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(AbancaConfiguration configuration) {
        this.configuration = configuration;
    }

    private String getTokenFromStorage() {
        return getConfiguration().getAuthKey();
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    public RequestBuilder createRequestInSession(URL url) {
        return createRequest(url)
                .header(AbancaConstants.HeaderKeys.AUTH_KEY, getTokenFromStorage());
    }

    public AccountsResponse fetchAccounts() {
        return createRequestInSession(AbancaConstants.Urls.ACCOUNTS).get(AccountsResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> fetchTranscations(
            TransactionalAccount account, String key) {

        URL url =
                Optional.ofNullable(key)
                        .map(s -> new URL(AbancaConstants.Urls.BASE_URL + key))
                        .orElseGet(
                                () ->
                                        AbancaConstants.Urls.TRANSACTIONS.parameter(
                                                AbancaConstants.UrlParameters.ACCOUNT_ID,
                                                account.getApiIdentifier()));

        return createRequestInSession(url).get(TransactionsResponse.class);
    }
}
