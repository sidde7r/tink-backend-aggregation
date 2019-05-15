package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia;

import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.configuration.SkandiaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class SkandiaApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private SkandiaConfiguration configuration;

    public SkandiaApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    private SkandiaConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(SkandiaConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromStorage();

        return createRequest(url).addBearerToken(authToken);
    }

    private RequestBuilder createFetchingRequest(URL url) {
        return createRequest(url)
                .header(HeaderKeys.X_REQUEST_ID, UUID.randomUUID())
                .header(HeaderKeys.CONSENT_ID, configuration.getConsentId());
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public GetAccountsResponse getAccounts() {
        return createFetchingRequest(Urls.GET_ACCOUNTS).get(GetAccountsResponse.class);
    }

    public GetBalancesResponse getBalances(String resourceId) {
        return createFetchingRequest(Urls.GET_BALANCES.parameter(IdTags.ACCOUNT_ID, resourceId))
                .get(GetBalancesResponse.class);
    }

    public GetTransactionsResponse getTransactions(TransactionalAccount account, String status) {
        return createFetchingRequest(
                        Urls.GET_TRANSACTIONS.parameter(
                                IdTags.ACCOUNT_ID, account.getApiIdentifier()))
                .queryParam(QueryKeys.BOOKING_STATUS, status)
                .get(GetTransactionsResponse.class);
    }
}
