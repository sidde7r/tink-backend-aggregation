package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BbvaConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BbvaConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BbvaConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BbvaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BbvaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.configuration.BbvaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.rpc.BbvaAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.rpc.BbvaDetailedAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.rpc.BbvaTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import tink.org.apache.http.HttpHeaders;

public final class BbvaApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private BbvaConfiguration configuration;

    public BbvaApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public void setConfiguration(BbvaConfiguration configuration) {
        this.configuration = configuration;
    }

    public BbvaConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    private RequestBuilder createTokenRequest(String url, String grantType) {
        final String redirectUri = getConfiguration().getRedirectUrl();
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();

        return client.request(url)
                .addBasicAuth(clientId, clientSecret)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .queryParam(QueryKeys.GRANT_TYPE, grantType);
    }

    private RequestBuilder createRequestInSession(URL url) {
        return client.request(url)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .addBearerToken(getTokenFromSession());
    }

    private RequestBuilder createRequestInSession(String url) {
        return createRequestInSession(new URL(url));
    }

    public PaginatorResponse fetchTransactions(TransactionalAccount account, int page) {
        final String baseApiUrl = getConfiguration().getBaseApiUrl();
        final String url =
                baseApiUrl + String.format(Urls.ACCOUNT_TRANSACTIONS, account.getApiIdentifier());

        return createRequestInSession(url)
                .queryParam(QueryKeys.PAGINATION, String.valueOf(page))
                .get(BbvaTransactionsResponse.class);
    }

    public BbvaDetailedAccountResponse fetchAccountDetails(String id) {
        final String baseApiUrl = getConfiguration().getBaseApiUrl();
        final String url = baseApiUrl + String.format(Urls.ACCOUNT, id);

        return createRequestInSession(url).get(BbvaDetailedAccountResponse.class);
    }

    public BbvaAccountsResponse fetchAccounts() {
        final String baseApiUrl = getConfiguration().getBaseApiUrl();

        return createRequestInSession(baseApiUrl + Urls.ACCOUNTS).get(BbvaAccountsResponse.class);
    }

    public URL getAuthorizeUrl(String state) {
        final String baseAuthUrl = getConfiguration().getBaseAuthUrl();
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();

        return client.request(baseAuthUrl + Urls.OAUTH)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.CLIENT_ID, clientId)
                .queryParam(QueryKeys.REDIRECT_URI, clientSecret)
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                .getUrl();
    }

    public OAuth2Token getToken(String code) {
        final String baseAuthUrl = getConfiguration().getBaseAuthUrl();

        return createTokenRequest(baseAuthUrl + Urls.TOKEN, QueryValues.GRANT_TYPE)
                .queryParam(QueryKeys.CODE, code)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(TokenResponse.class)
                .toTinkToken();
    }

    public OAuth2Token getRefreshToken(String refreshToken) {
        final String baseAuthUrl = getConfiguration().getBaseAuthUrl();
        final RefreshRequest refreshRequest = new RefreshRequest(refreshToken);

        return createTokenRequest(baseAuthUrl + Urls.TOKEN, QueryValues.REFRESH_TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, refreshRequest.toForm())
                .toTinkToken();
    }

    public void setTokenToSession(OAuth2Token accessToken) {
        sessionStorage.put(StorageKeys.TOKEN, accessToken);
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_TOKEN));
    }
}
