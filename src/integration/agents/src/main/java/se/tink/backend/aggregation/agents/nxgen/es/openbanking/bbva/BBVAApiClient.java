package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva;

import java.util.Base64;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BBVAConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BBVAConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.rpc.BBVAAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.rpc.BBVADetailedAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.rpc.BBVATransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import tink.org.apache.http.HttpHeaders;

public final class BBVAApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public BBVAApiClient(
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
                        BBVAConstants.HeaderKeys.AUTHORIZATION,
                        getHeaderFormattedTokenFromSession())
                .queryParam(BBVAConstants.QueryKeys.PAGINATION, String.valueOf(page))
                .get(BBVATransactionsResponse.class);
    }

    public BBVADetailedAccountResponse fetchAccountDetails(String id) {
        return client.request(
                        new URL(
                                persistentStorage.get(StorageKeys.BASE_API_URL)
                                        + String.format(Urls.ACCOUNT, id)))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(
                        BBVAConstants.HeaderKeys.AUTHORIZATION,
                        getHeaderFormattedTokenFromSession())
                .get(BBVADetailedAccountResponse.class);
    }

    public BBVAAccountsResponse fetchAccounts() {
        return client.request(
                        new URL(
                                persistentStorage.get(BBVAConstants.StorageKeys.BASE_API_URL)
                                        + BBVAConstants.Urls.ACCOUNTS))
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .header(
                        BBVAConstants.HeaderKeys.AUTHORIZATION,
                        getHeaderFormattedTokenFromSession())
                .get(BBVAAccountsResponse.class);
    }

    public URL getAuthorizeUrl(String state) {
        return client.request(
                        new URL(
                                persistentStorage.get(BBVAConstants.StorageKeys.BASE_AUTH_URL)
                                        + BBVAConstants.Urls.OAUTH))
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML)
                .queryParam(
                        BBVAConstants.QueryKeys.RESPONSE_TYPE,
                        BBVAConstants.QueryValues.RESPONSE_TYPE)
                .queryParam(
                        BBVAConstants.QueryKeys.CLIENT_ID,
                        persistentStorage.get(BBVAConstants.StorageKeys.CLIENT_ID))
                .queryParam(
                        BBVAConstants.QueryKeys.REDIRECT_URI,
                        persistentStorage.get(BBVAConstants.StorageKeys.REDIRECT_URI))
                .queryParam(BBVAConstants.QueryKeys.STATE, state)
                .queryParam(BBVAConstants.QueryKeys.SCOPE, BBVAConstants.QueryValues.SCOPE)
                .getUrl();
    }

    public OAuth2Token getToken(String code) {
        return client.request(
                        new URL(
                                persistentStorage.get(BBVAConstants.StorageKeys.BASE_AUTH_URL)
                                        + BBVAConstants.Urls.TOKEN))
                .queryParam(
                        BBVAConstants.QueryKeys.GRANT_TYPE, BBVAConstants.QueryValues.GRANT_TYPE)
                .queryParam(BBVAConstants.QueryKeys.CODE, code)
                .queryParam(
                        BBVAConstants.QueryKeys.REDIRECT_URI,
                        persistentStorage.get(BBVAConstants.StorageKeys.REDIRECT_URI))
                .header(
                        BBVAConstants.HeaderKeys.AUTHORIZATION,
                        String.format(
                                BBVAConstants.HeaderValues.AUTHORIZATION_RESPONSE,
                                getBase64Credentials()))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(TokenResponse.class)
                .toTinkToken();
    }

    public OAuth2Token getRefreshToken(String refreshToken) {
        RefreshRequest refreshRequest = new RefreshRequest(refreshToken);

        return client.request(
                        new URL(
                                persistentStorage.get(BBVAConstants.StorageKeys.BASE_AUTH_URL)
                                        + BBVAConstants.Urls.TOKEN))
                .queryParam(
                        BBVAConstants.QueryKeys.GRANT_TYPE, BBVAConstants.QueryValues.REFRESH_TOKEN)
                .queryParam(
                        BBVAConstants.QueryKeys.REDIRECT_URI,
                        persistentStorage.get(BBVAConstants.StorageKeys.REDIRECT_URI))
                .header(
                        BBVAConstants.HeaderKeys.AUTHORIZATION,
                        String.format(
                                BBVAConstants.HeaderValues.AUTHORIZATION_RESPONSE,
                                getBase64Credentials()))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, refreshRequest.toForm())
                .toTinkToken();
    }

    public String getBase64Credentials() {
        return Base64.getEncoder()
                .encodeToString(
                        (persistentStorage.get(BBVAConstants.StorageKeys.CLIENT_ID)
                                        + ":"
                                        + persistentStorage.get(
                                                BBVAConstants.StorageKeys.CLIENT_SECRET))
                                .getBytes());
    }

    public void setTokenToSession(OAuth2Token accessToken) {
        sessionStorage.put(BBVAConstants.StorageKeys.TOKEN, accessToken);
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(BBVAConstants.StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(BBVAConstants.Exceptions.MISSING_TOKEN));
    }

    private String getHeaderFormattedTokenFromSession() {
        return String.format(
                BBVAConstants.HeaderValues.AUTHORIZATION, getTokenFromSession().getAccessToken());
    }
}
