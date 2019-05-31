package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.configuration.AbancaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class AbancaApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private AbancaConfiguration configuration;

    public AbancaApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    private AbancaConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(AbancaConfiguration configuration) {
        this.configuration = configuration;
    }

    private String getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, String.class)
                .orElseThrow(() -> new IllegalStateException("Token not found!"));
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    public RequestBuilder createRequestInSession(URL url) {
        return createRequest(url)
                .header(AbancaConstants.HeaderKeys.AUTH_KEY, configuration.getAuthKey())
                .header(
                        AbancaConstants.HeaderKeys.AUTHORIZATION,
                        AbancaConstants.HeaderValues.TOKEN_PREFIX + getTokenFromStorage());
    }

    public AccountsResponse fetchAccounts() {
        return createRequestInSession(AbancaConstants.Urls.ACCOUNTS).get(AccountsResponse.class);
    }

    public BalanceResponse fetchBalance(String accountId) {
        return createRequestInSession(
                        Urls.BALANCE.parameter(AbancaConstants.UrlParameters.ACCOUNT_ID, accountId))
                .get(BalanceResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> fetchTranscations(
            TransactionalAccount account, String key) {

        URL url =
                Optional.ofNullable(key)
                        .map(s -> new URL(AbancaConstants.Urls.BASE_API_URL + key))
                        .orElseGet(
                                () ->
                                        AbancaConstants.Urls.TRANSACTIONS.parameter(
                                                AbancaConstants.UrlParameters.ACCOUNT_ID,
                                                account.getApiIdentifier()));

        return createRequestInSession(url).get(TransactionsResponse.class);
    }

    public void authenticate(Credentials credentials) {

        TokenRequest tokenRequest =
                new TokenRequest(
                        AbancaConstants.FormValues.APPLICATION,
                        AbancaConstants.FormValues.GRANT_TYPE,
                        credentials.getField(Key.USERNAME),
                        credentials.getField(Key.PASSWORD),
                        configuration.getApiKey());

        TokenResponse tokenResponse =
                client.request(Urls.TOKEN)
                        .header(HeaderKeys.AUTH_KEY, getConfiguration().getAuthKey())
                        .type(MediaType.APPLICATION_FORM_URLENCODED)
                        .post(TokenResponse.class, tokenRequest.toData());

        persistentStorage.put(StorageKeys.OAUTH_TOKEN, tokenResponse.getAccessToken());
    }
}
