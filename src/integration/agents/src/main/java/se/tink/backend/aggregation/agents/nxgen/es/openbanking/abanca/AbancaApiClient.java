package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca;

import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.UrlParameters;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.AbancaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.configuration.AbancaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class AbancaApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final AbancaConfiguration configuration;
    private final SessionStorage sessionStorage;

    public AbancaApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            AbancaConfiguration abancaConfiguration,
            SessionStorage sessionStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.configuration = abancaConfiguration;
        this.sessionStorage = sessionStorage;
    }

    private AbancaConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Token not found!"));
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    public RequestBuilder createRequestInSession(URL url) {
        return createRequest(url)
                .header(HeaderKeys.AUTH_KEY, configuration.getApiKey())
                .addBearerToken(getTokenFromSession());
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

    public URL getAuthorizeUrl(String state) {
        return client.request(
                        Urls.AUTHORIZATION.parameter(
                                UrlParameters.CLIENT_ID, configuration.getClientId()))
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                .queryParam(QueryKeys.REDIRECT_URI, configuration.getRedirectUrl())
                .queryParam(QueryKeys.STATE, state)
                .getUrl();
    }

    public OAuth2Token getToken(TokenRequest tokenRequest) {
        return client.request(Urls.TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .header(HeaderKeys.AUTH_KEY, configuration.getApiKey())
                .body(tokenRequest, MediaType.APPLICATION_JSON_TYPE)
                .post(TokenResponse.class)
                .toTinkToken();
    }

    public void setTokenToSession(OAuth2Token accessToken) {
        sessionStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }
}
