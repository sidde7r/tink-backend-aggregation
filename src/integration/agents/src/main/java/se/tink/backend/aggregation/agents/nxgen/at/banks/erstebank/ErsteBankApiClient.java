package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank;

import java.util.NoSuchElementException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.password.authenticator.entity.EncryptionValuesEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.password.authenticator.entity.TokenEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.sidentity.authenticator.rpc.PollResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ErsteBankApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;

    public ErsteBankApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;

        // This is required, otherwise TinkHttpClient throws exception due to deeplink redirect
        client.setFollowRedirects(false);
    }

    private RequestBuilder getRequestWithHeaders(
            String baseUrl, String resource, String accept, String bearer) {
        return getRequest(baseUrl, resource, accept)
                .header(
                        ErsteBankConstants.Headers.X_MOBILE_APP_ID,
                        ErsteBankConstants.Headers.X_MOBILE_APP_ID_IOS)
                .header(
                        ErsteBankConstants.Headers.ENVIROMENT,
                        ErsteBankConstants.Headers.ENVIROMENT_PROD)
                .header(ErsteBankConstants.Headers.AUTHORIZATION, bearer)
                .header(
                        ErsteBankConstants.Headers.X_APP_ID,
                        ErsteBankConstants.Headers.X_APP_ID_TRANSACTIONAPP)
                .header(ErsteBankConstants.Headers.X_REQUEST_ID, generateGuid());
    }

    private RequestBuilder getRequest(String baseUrl, String resource, String accept) {
        return client.request(new URL(baseUrl + resource)).header(HttpHeaders.ACCEPT, accept);
    }

    private RequestBuilder getRequest(
            String baseUrl, String resource, String accept, String redirectUrl) {
        return getRequest(baseUrl, resource, accept)
                .queryParam(
                        ErsteBankConstants.QueryParams.RESPONSE_TYPE,
                        ErsteBankConstants.QueryParams.RESPONSE_TYPE_TOKEN)
                .queryParam(
                        ErsteBankConstants.QueryParams.CLIENT_ID,
                        ErsteBankConstants.QueryParams.CLIENT_ID_TRANSACTIONAPP)
                .queryParam(ErsteBankConstants.QueryParams.REDIRECT_URI, redirectUrl);
    }

    private EncryptionValuesEntity GetEncryptionValues(String username) throws LoginException {
        String html =
                getRequest(
                                ErsteBankConstants.Urls.LOGIN_BASE,
                                ErsteBankConstants.Urls.OAUTH,
                                ErsteBankConstants.QueryParams.SPARKASSE_ACCEPT,
                                ErsteBankConstants.QueryParams.REDIRECT_URI_AUTHENTICATION)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                        .post(String.class, ErsteBankConstants.Body.USERNAME + username);

        return ErsteBankCryptoUtil.getEncryptionValues(html);
    }

    private void getCookies() {
        HttpResponse response =
                getRequest(
                                ErsteBankConstants.Urls.LOGIN_BASE,
                                ErsteBankConstants.Urls.OAUTH,
                                ErsteBankConstants.QueryParams.SPARKASSE_ACCEPT,
                                ErsteBankConstants.QueryParams.REDIRECT_URI_AUTHENTICATION)
                        .get(HttpResponse.class);
    }

    private void sendJavascriptEnabled() {
        getRequest(
                        ErsteBankConstants.Urls.LOGIN_BASE,
                        ErsteBankConstants.Urls.OAUTH,
                        ErsteBankConstants.QueryParams.SPARKASSE_ACCEPT,
                        ErsteBankConstants.QueryParams.REDIRECT_URI_AUTHENTICATION)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .post(HttpResponse.class, ErsteBankConstants.Body.JAVASCRIPT_ENABLED);
    }

    public EncryptionValuesEntity getEncryptionValues(String username) throws LoginException {
        getCookies();
        sendJavascriptEnabled();
        return GetEncryptionValues(username);
    }

    public HttpResponse sendPassword(String rsa) {

        Form form =
                Form.builder()
                        .put(ErsteBankConstants.Body.RSA_ENCRYPTED, rsa)
                        .put(
                                ErsteBankConstants.Body.AUTHENTICATION_METHOD,
                                ErsteBankConstants.Body.AUTHENTICATION_METHOD_PASSWORD)
                        .build();

        return getRequest(
                        ErsteBankConstants.Urls.LOGIN_BASE,
                        ErsteBankConstants.Urls.OAUTH,
                        ErsteBankConstants.QueryParams.SPARKASSE_ACCEPT,
                        ErsteBankConstants.QueryParams.REDIRECT_URI_AUTHENTICATION)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .post(HttpResponse.class, form.serialize());
    }

    public TokenEntity getTokenFromStorage() {
        return persistentStorage
                .get(ErsteBankConstants.Storage.TOKEN_ENTITY, TokenEntity.class)
                .orElseThrow(() -> new NoSuchElementException("Token missing"));
    }

    private String generateGuid() {
        return java.util.UUID.randomUUID().toString().toUpperCase();
    }

    public AccountResponse fetchAccounts() {
        TokenEntity token = getTokenFromStorage();
        String bearer = ErsteBankConstants.Headers.BEARER + token.getToken();

        return getRequestWithHeaders(
                        ErsteBankConstants.Urls.GEORGE_GO_BASE,
                        ErsteBankConstants.Urls.ACCOUNT,
                        ErsteBankConstants.Headers.ACCEPT,
                        bearer)
                .queryParam(
                        ErsteBankConstants.QueryParams.FEATURES,
                        ErsteBankConstants.QueryParams.FEATURES_ALL)
                .get(AccountResponse.class);
    }

    private String getTransactionUrl(String url) {
        return String.format(ErsteBankConstants.Patterns.TRANSACTION_FORMAT, url);
    }

    public TransactionsResponse fetchTransactions(int page, String accountUrl) {
        TokenEntity token = getTokenFromStorage();
        String bearer = ErsteBankConstants.Headers.BEARER + token.getToken();
        String resource = getTransactionUrl(accountUrl);

        return getRequestWithHeaders(
                        ErsteBankConstants.Urls.GEORGE_GO_BASE,
                        resource,
                        ErsteBankConstants.Headers.ACCEPT,
                        bearer)
                .queryParam(ErsteBankConstants.QueryParams.PAGE, Integer.toString(page))
                .queryParam(
                        ErsteBankConstants.QueryParams.FEATURES,
                        ErsteBankConstants.QueryParams.FEATURES_ALL)
                .get(TransactionsResponse.class);
    }

    public void logout() {
        TokenEntity token = getTokenFromStorage();
        String bearer = ErsteBankConstants.Headers.BEARER + token.getToken();

        getRequestWithHeaders(
                        ErsteBankConstants.Urls.SPARKASSE_BASE,
                        ErsteBankConstants.Urls.LOGOUT,
                        MediaType.APPLICATION_JSON,
                        bearer)
                .delete(HttpResponse.class);
    }

    public void saveToken(TokenEntity tokenEntity) {
        persistentStorage.put(ErsteBankConstants.Storage.TOKEN_ENTITY, tokenEntity);
    }

    public boolean tokenExists() {
        return persistentStorage.containsKey(ErsteBankConstants.Storage.TOKEN_ENTITY);
    }

    // Sidentity

    public PollResponse pollStatus() {
        return getRequest(ErsteBankConstants.Urls.LOGIN_BASE, ErsteBankConstants.Urls.POLL, "*/*")
                .queryParam(
                        ErsteBankConstants.QueryParams.CLIENT_ID,
                        ErsteBankConstants.QueryParams.CLIENT_ID_TRANSACTIONAPP)
                .post(PollResponse.class);
    }

    public String getSidentityVerificationCode(String username) throws LoginException {
        getCookies();
        sendJavascriptEnabled();

        String html =
                getRequest(
                                ErsteBankConstants.Urls.LOGIN_BASE,
                                ErsteBankConstants.Urls.OAUTH,
                                ErsteBankConstants.QueryParams.SPARKASSE_ACCEPT,
                                ErsteBankConstants.QueryParams.REDIRECT_URI_AUTHENTICATION)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                        .post(String.class, ErsteBankConstants.Body.USERNAME + username);

        return ErsteBankCryptoUtil.getSidentityCode(html);
    }

    public TokenEntity getSidentityToken() throws LoginException {
        HttpResponse response =
                getRequest(
                                ErsteBankConstants.Urls.LOGIN_BASE,
                                ErsteBankConstants.Urls.OAUTH,
                                ErsteBankConstants.QueryParams.SPARKASSE_ACCEPT,
                                ErsteBankConstants.QueryParams.REDIRECT_URI_AUTHENTICATION)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                        .post(HttpResponse.class);

        return ErsteBankCryptoUtil.getTokenFromResponse(response);
    }
}
