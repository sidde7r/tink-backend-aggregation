package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank;

import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.QueryParams.ACCOUNT_ID;
import static se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.Urls.BASE_URL;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.BasicAuthParams;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.entities.TokenEntity;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc.PasswordLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc.RedirectLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc.RedirectRefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class DemobankApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final String callbackUri;

    public DemobankApiClient(
            TinkHttpClient client, SessionStorage sessionStorage, String callbackUri) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.callbackUri = callbackUri;
    }

    public OAuth2Token getToken(String code) {
        return createRequest(fetchBaseUrl().concat(Urls.OAUTH_TOKEN))
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .addBasicAuth(BasicAuthParams.CLIENT_ID, BasicAuthParams.CLIENT_SECRET)
                .post(TokenEntity.class, new RedirectLoginRequest(code, callbackUri).toData())
                .toOAuth2Token();
    }

    public void setTokenToSession(OAuth2Token accessToken) {
        sessionStorage.put(StorageKeys.OAUTH2_TOKEN, accessToken);
    }

    public OAuth2Token refreshToken(String refreshToken) {
        return createRequest(fetchBaseUrl().concat(Urls.OAUTH_TOKEN))
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .addBasicAuth(BasicAuthParams.CLIENT_ID, BasicAuthParams.CLIENT_SECRET)
                .post(TokenEntity.class, new RedirectRefreshTokenRequest(refreshToken).toData())
                .toOAuth2Token();
    }

    public FetchAccountResponse fetchAccounts() {
        final URL url = fetchBaseUrl().concat(Urls.ACCOUNTS);

        return createRequestInSession(url, getOauth2TokenFromStorage())
                .get(FetchAccountResponse.class);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE);
    }

    private RequestBuilder createRequestInSession(URL url, OAuth2Token token) {
        return createRequest(url).addBearerToken(token);
    }

    public URL fetchBaseUrl() {
        return new URL(BASE_URL);
    }

    public OAuth2Token login(String username, String password) {

        return createRequest(fetchBaseUrl().concat(Urls.OAUTH_TOKEN))
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .addBasicAuth(BasicAuthParams.CLIENT_ID, BasicAuthParams.CLIENT_SECRET)
                .post(TokenEntity.class, new PasswordLoginRequest(username, password).toData())
                .toOAuth2Token();
    }

    public FetchTransactionsResponse fetchTransactions(String accountId) {
        final URL url = fetchBaseUrl().concat(Urls.TRANSACTIONS).parameter(ACCOUNT_ID, accountId);
        return createRequestInSession(url, getOauth2TokenFromStorage())
                .get(FetchTransactionsResponse.class);
    }

    public OAuth2Token getOauth2TokenFromStorage() {
        return sessionStorage
                .get(StorageKeys.OAUTH2_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Couldn't find token from session storage"));
    }
}
