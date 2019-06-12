package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius;

import java.util.Optional;
import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.configuration.BelfiusConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.entity.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class BelfiusApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private BelfiusConfiguration configuration;

    public BelfiusApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    private BelfiusConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(BelfiusConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url);
    }

    private RequestBuilder createRequestInSession(URL url) {

        return createRequest(url)
                .header(HeaderKeys.CLIENT_ID, configuration.getClientId())
                .header(HeaderKeys.ACCEPT, HeaderValues.ACCEPT)
                .header(HeaderKeys.REDIRECT_URI, configuration.getRedirectUrl())
                .acceptLanguage(HeaderValues.ACCEPT_LANGUAGE)
                .header(HeaderKeys.REQUEST_ID, UUID.randomUUID())
                .header(HeaderKeys.AUTHORIZATION, HeaderValues.AUTHORIZATION);
    }

    public FetchAccountResponse fetchAccountById(String id) {
        return createRequestInSession(
                        new URL(configuration.getBaseUrl() + Urls.FETCH_ACCOUNT_PATH + id))
                .get(FetchAccountResponse.class);
    }

    public TransactionKeyPaginatorResponse<URL> fetchTransactionsForAccount(String id) {
        URL url =
                new URL(
                        configuration.getBaseUrl()
                                + String.format(Urls.FETCH_TRANSACTIONS_PATH, id));

        return createRequestInSession(url).get(FetchTransactionsResponse.class);
    }
}
