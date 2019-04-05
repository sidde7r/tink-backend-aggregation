package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva;

import java.util.Base64;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BbvaConstants.Exceptions;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BbvaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BbvaConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BbvaConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BbvaConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BbvaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BbvaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.rpc.BbvaAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.rpc.BbvaDetailedAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.rpc.BbvaTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import tink.org.apache.http.HttpHeaders;

public final class BbvaApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public BbvaApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    private RequestBuilder createTokenRequest(URL url, String grantType) {
        return client.request(url)
                .header(
                        HeaderKeys.AUTHORIZATION,
                        String.format(HeaderValues.AUTHORIZATION_RESPONSE, getBase64Credentials()))
                .queryParam(QueryKeys.REDIRECT_URI, persistentStorage.get(StorageKeys.REDIRECT_URI))
                .queryParam(QueryKeys.GRANT_TYPE, grantType);
    }

    private RequestBuilder createRequestInSession(URL url) {
        return client.request(url)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(HeaderKeys.AUTHORIZATION, getHeaderFormattedTokenFromSession());
    }

    public PaginatorResponse fetchTransactions(TransactionalAccount account, int page) {
        final URL url =
                new URL(
                        persistentStorage.get(StorageKeys.BASE_API_URL)
                                + String.format(
                                        Urls.ACCOUNT_TRANSACTIONS, account.getApiIdentifier()));

        return createRequestInSession(url)
                .queryParam(QueryKeys.PAGINATION, String.valueOf(page))
                .get(BbvaTransactionsResponse.class);
    }

    public BbvaDetailedAccountResponse fetchAccountDetails(String id) {
        final URL url =
                new URL(
                        persistentStorage.get(StorageKeys.BASE_API_URL)
                                + String.format(Urls.ACCOUNT, id));

        return createRequestInSession(url).get(BbvaDetailedAccountResponse.class);
    }

    public BbvaAccountsResponse fetchAccounts() {
        final URL url = new URL(persistentStorage.get(StorageKeys.BASE_API_URL) + Urls.ACCOUNTS);

        return createRequestInSession(url).get(BbvaAccountsResponse.class);
    }

    public URL getAuthorizeUrl(String state) {
        final URL url = new URL(persistentStorage.get(StorageKeys.BASE_AUTH_URL) + Urls.OAUTH);

        return client.request(url)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.CLIENT_ID, persistentStorage.get(StorageKeys.CLIENT_ID))
                .queryParam(QueryKeys.REDIRECT_URI, persistentStorage.get(StorageKeys.REDIRECT_URI))
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                .getUrl();
    }

    public OAuth2Token getToken(String code) {
        final URL url = new URL(persistentStorage.get(StorageKeys.BASE_AUTH_URL) + Urls.TOKEN);

        return createTokenRequest(url, QueryValues.GRANT_TYPE)
                .queryParam(QueryKeys.CODE, code)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(TokenResponse.class)
                .toTinkToken();
    }

    public OAuth2Token getRefreshToken(String refreshToken) {
        final URL url = new URL(persistentStorage.get(StorageKeys.BASE_AUTH_URL) + Urls.TOKEN);
        final RefreshRequest refreshRequest = new RefreshRequest(refreshToken);

        return createTokenRequest(url, QueryValues.REFRESH_TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, refreshRequest.toForm())
                .toTinkToken();
    }

    public String getBase64Credentials() {
        final String credentialString =
                persistentStorage.get(StorageKeys.CLIENT_ID)
                        + ":"
                        + persistentStorage.get(StorageKeys.CLIENT_SECRET);

        return Base64.getEncoder().encodeToString(credentialString.getBytes());
    }

    public void setTokenToSession(OAuth2Token accessToken) {
        sessionStorage.put(StorageKeys.TOKEN, accessToken);
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException(Exceptions.MISSING_TOKEN));
    }

    private String getHeaderFormattedTokenFromSession() {
        return String.format(HeaderValues.AUTHORIZATION, getTokenFromSession().getAccessToken());
    }
}
