package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva;

import java.util.Base64;
import javax.ws.rs.core.MediaType;
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

    public PaginatorResponse fetchTransactions(TransactionalAccount account, int page) {
        return client.request(
                        new URL(
                                persistentStorage.get(StorageKeys.BASE_API_URL)
                                        + String.format(
                                                Urls.ACCOUNT_TRANSACTIONS,
                                                account.getApiIdentifier())))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(
                        BbvaConstants.HeaderKeys.AUTHORIZATION,
                        getHeaderFormattedTokenFromSession())
                .queryParam(BbvaConstants.QueryKeys.PAGINATION, String.valueOf(page))
                .get(BbvaTransactionsResponse.class);
    }

    public BbvaDetailedAccountResponse fetchAccountDetails(String id) {
        return client.request(
                        new URL(
                                persistentStorage.get(StorageKeys.BASE_API_URL)
                                        + String.format(Urls.ACCOUNT, id)))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(
                        BbvaConstants.HeaderKeys.AUTHORIZATION,
                        getHeaderFormattedTokenFromSession())
                .get(BbvaDetailedAccountResponse.class);
    }

    public BbvaAccountsResponse fetchAccounts() {
        return client.request(
                        new URL(
                                persistentStorage.get(BbvaConstants.StorageKeys.BASE_API_URL)
                                        + BbvaConstants.Urls.ACCOUNTS))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(
                        BbvaConstants.HeaderKeys.AUTHORIZATION,
                        getHeaderFormattedTokenFromSession())
                .get(BbvaAccountsResponse.class);
    }

    public URL getAuthorizeUrl(String state) {
        return client.request(
                        new URL(
                                persistentStorage.get(BbvaConstants.StorageKeys.BASE_AUTH_URL)
                                        + BbvaConstants.Urls.OAUTH))
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML)
                .queryParam(
                        BbvaConstants.QueryKeys.RESPONSE_TYPE,
                        BbvaConstants.QueryValues.RESPONSE_TYPE)
                .queryParam(
                        BbvaConstants.QueryKeys.CLIENT_ID,
                        persistentStorage.get(BbvaConstants.StorageKeys.CLIENT_ID))
                .queryParam(
                        BbvaConstants.QueryKeys.REDIRECT_URI,
                        persistentStorage.get(BbvaConstants.StorageKeys.REDIRECT_URI))
                .queryParam(BbvaConstants.QueryKeys.STATE, state)
                .queryParam(BbvaConstants.QueryKeys.SCOPE, BbvaConstants.QueryValues.SCOPE)
                .getUrl();
    }

    public OAuth2Token getToken(String code) {
        return client.request(
                        new URL(
                                persistentStorage.get(BbvaConstants.StorageKeys.BASE_AUTH_URL)
                                        + BbvaConstants.Urls.TOKEN))
                .queryParam(
                        BbvaConstants.QueryKeys.GRANT_TYPE, BbvaConstants.QueryValues.GRANT_TYPE)
                .queryParam(BbvaConstants.QueryKeys.CODE, code)
                .queryParam(
                        BbvaConstants.QueryKeys.REDIRECT_URI,
                        persistentStorage.get(BbvaConstants.StorageKeys.REDIRECT_URI))
                .header(
                        BbvaConstants.HeaderKeys.AUTHORIZATION,
                        String.format(
                                BbvaConstants.HeaderValues.AUTHORIZATION_RESPONSE,
                                getBase64Credentials()))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(TokenResponse.class)
                .toTinkToken();
    }

    public OAuth2Token getRefreshToken(String refreshToken) {
        RefreshRequest refreshRequest = new RefreshRequest(refreshToken);

        return client.request(
                        new URL(
                                persistentStorage.get(BbvaConstants.StorageKeys.BASE_AUTH_URL)
                                        + BbvaConstants.Urls.TOKEN))
                .queryParam(
                        BbvaConstants.QueryKeys.GRANT_TYPE, BbvaConstants.QueryValues.REFRESH_TOKEN)
                .queryParam(
                        BbvaConstants.QueryKeys.REDIRECT_URI,
                        persistentStorage.get(BbvaConstants.StorageKeys.REDIRECT_URI))
                .header(
                        BbvaConstants.HeaderKeys.AUTHORIZATION,
                        String.format(
                                BbvaConstants.HeaderValues.AUTHORIZATION_RESPONSE,
                                getBase64Credentials()))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, refreshRequest.toForm())
                .toTinkToken();
    }

    public String getBase64Credentials() {
        return Base64.getEncoder()
                .encodeToString(
                        (persistentStorage.get(BbvaConstants.StorageKeys.CLIENT_ID)
                                        + ":"
                                        + persistentStorage.get(
                                                BbvaConstants.StorageKeys.CLIENT_SECRET))
                                .getBytes());
    }

    public void setTokenToSession(OAuth2Token accessToken) {
        sessionStorage.put(BbvaConstants.StorageKeys.TOKEN, accessToken);
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(BbvaConstants.StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(BbvaConstants.Exceptions.MISSING_TOKEN));
    }

    private String getHeaderFormattedTokenFromSession() {
        return String.format(
                BbvaConstants.HeaderValues.AUTHORIZATION, getTokenFromSession().getAccessToken());
    }
}
