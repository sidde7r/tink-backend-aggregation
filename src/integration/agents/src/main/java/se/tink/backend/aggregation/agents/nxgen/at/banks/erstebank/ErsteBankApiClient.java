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
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ErsteBankApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;

    public ErsteBankApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;

        //This is required, otherwise TinkHttpClient throws exception due to deeplink redirect
        client.setFollowRedirects(false);
    }

    private RequestBuilder getRequestWithHeaders(String baseUrl, String resource, String accept, String bearer) {
        return getRequest(baseUrl, resource, accept)
                .header(ErsteBankConstants.HEADERS.X_MOBILE_APP_ID, ErsteBankConstants.HEADERS.X_MOBILE_APP_ID_IOS)
                .header(ErsteBankConstants.HEADERS.ENVIROMENT, ErsteBankConstants.HEADERS.ENVIROMENT_PROD)
                .header(ErsteBankConstants.HEADERS.AUTHORIZATION, bearer)
                .header(ErsteBankConstants.HEADERS.X_APP_ID, ErsteBankConstants.HEADERS.X_APP_ID_TRANSACTIONAPP)
                .header(ErsteBankConstants.HEADERS.X_REQUEST_ID, generateGuid());
    }

    private RequestBuilder getRequest(String baseUrl, String resource, String accept) {
        return client.request(new URL(baseUrl + resource))
                .header(HttpHeaders.ACCEPT, accept);
    }

    private RequestBuilder getRequest(String baseUrl, String resource, String accept, String redirectUrl) {
        return getRequest(baseUrl, resource, accept)
                .queryParam(ErsteBankConstants.QUERYPARAMS.RESPONSE_TYPE,
                        ErsteBankConstants.QUERYPARAMS.RESPONSE_TYPE_TOKEN)
                .queryParam(ErsteBankConstants.QUERYPARAMS.CLIENT_ID,
                        ErsteBankConstants.QUERYPARAMS.CLIENT_ID_TRANSACTIONAPP)
                .queryParam(ErsteBankConstants.QUERYPARAMS.REDIRECT_URI, redirectUrl);
    }

    private EncryptionValuesEntity GetEncryptionValues(String username) throws LoginException {
        String html = getRequest(ErsteBankConstants.URLS.LOGIN_BASE, ErsteBankConstants.URLS.OAUTH,
                ErsteBankConstants.QUERYPARAMS.SPARKASSE_ACCEPT,
                ErsteBankConstants.QUERYPARAMS.REDIRECT_URI_AUTHENTICATION)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .post(String.class, ErsteBankConstants.BODY.USERNAME + username);

        return ErsteBankCryptoUtil.getEncryptionValues(html);
    }

    private void getCookies() {
        HttpResponse response = getRequest(ErsteBankConstants.URLS.LOGIN_BASE, ErsteBankConstants.URLS.OAUTH,
                ErsteBankConstants.QUERYPARAMS.SPARKASSE_ACCEPT,
                ErsteBankConstants.QUERYPARAMS.REDIRECT_URI_AUTHENTICATION).get(HttpResponse.class);
    }

    private void sendJavascriptEnabled() {
        getRequest(ErsteBankConstants.URLS.LOGIN_BASE, ErsteBankConstants.URLS.OAUTH,
                ErsteBankConstants.QUERYPARAMS.SPARKASSE_ACCEPT,
                ErsteBankConstants.QUERYPARAMS.REDIRECT_URI_AUTHENTICATION)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .post(HttpResponse.class, ErsteBankConstants.BODY.JAVASCRIPT_ENABLED);
    }

    public EncryptionValuesEntity getEncryptionValues(String username) throws LoginException {
        getCookies();
        sendJavascriptEnabled();
        return GetEncryptionValues(username);
    }

    public HttpResponse sendPassword(String rsa) {

        Form form = Form.builder()
                .put(ErsteBankConstants.BODY.RSA_ENCRYPTED, rsa)
                .put(ErsteBankConstants.BODY.AUTHENTICATION_METHOD,
                        ErsteBankConstants.BODY.AUTHENTICATION_METHOD_PASSWORD)
                .build();

        return getRequest(ErsteBankConstants.URLS.LOGIN_BASE, ErsteBankConstants.URLS.OAUTH,
                ErsteBankConstants.QUERYPARAMS.SPARKASSE_ACCEPT,
                ErsteBankConstants.QUERYPARAMS.REDIRECT_URI_AUTHENTICATION)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .post(HttpResponse.class, form.serialize());
    }

    public TokenEntity getTokenFromStorage() {
        return persistentStorage.get(ErsteBankConstants.STORAGE.TOKEN_ENTITY, TokenEntity.class)
                .orElseThrow(() -> new NoSuchElementException("Token missing"));
    }

    private String generateGuid() {
        return java.util.UUID.randomUUID().toString().toUpperCase();
    }

    public AccountResponse fetchAccounts() {
        TokenEntity token = getTokenFromStorage();
        String bearer = ErsteBankConstants.HEADERS.BEARER + token.getToken();

        return getRequestWithHeaders(ErsteBankConstants.URLS.GEORGE_GO_BASE,
                ErsteBankConstants.URLS.ACCOUNT, ErsteBankConstants.HEADERS.ACCEPT, bearer)
                .queryParam(ErsteBankConstants.QUERYPARAMS.FEATURES, ErsteBankConstants.QUERYPARAMS.FEATURES_ALL)
                .get(AccountResponse.class);
    }

    private String getTransactionUrl(String url) {
        return String.format(ErsteBankConstants.PATTERN.TRANSACTION_FORMAT, url);
    }

    public TransactionsResponse fetchTransactions(int page, String accountUrl) {
        TokenEntity token = getTokenFromStorage();
        String bearer = ErsteBankConstants.HEADERS.BEARER + token.getToken();
        String resource = getTransactionUrl(accountUrl);

        return getRequestWithHeaders(ErsteBankConstants.URLS.GEORGE_GO_BASE, resource,
                ErsteBankConstants.HEADERS.ACCEPT, bearer)
                .queryParam(ErsteBankConstants.QUERYPARAMS.PAGE, Integer.toString(page))
                .queryParam(ErsteBankConstants.QUERYPARAMS.FEATURES, ErsteBankConstants.QUERYPARAMS.FEATURES_ALL)
                .get(TransactionsResponse.class);
    }

    public void logout() {
        TokenEntity token = getTokenFromStorage();
        String bearer = ErsteBankConstants.HEADERS.BEARER + token.getToken();

        getRequestWithHeaders(ErsteBankConstants.URLS.SPARKASSE_BASE,
                ErsteBankConstants.URLS.LOGOUT, MediaType.APPLICATION_JSON, bearer)
                .delete(HttpResponse.class);
    }

    public void saveToken(TokenEntity tokenEntity) {
        persistentStorage.put(ErsteBankConstants.STORAGE.TOKEN_ENTITY, tokenEntity);
    }


    public boolean tokenExists() {
        return persistentStorage.containsKey(ErsteBankConstants.STORAGE.TOKEN_ENTITY);
    }

    // Sidentity

    public PollResponse pollStatus() {
        return getRequest(ErsteBankConstants.URLS.LOGIN_BASE, ErsteBankConstants.URLS.POLL, "*/*")
                .post(PollResponse.class);
    }

    public String getSidentityVerificationCode(String username) throws LoginException {
        getCookies();
        sendJavascriptEnabled();

        String html = getRequest(ErsteBankConstants.URLS.LOGIN_BASE, ErsteBankConstants.URLS.OAUTH,
                ErsteBankConstants.QUERYPARAMS.SPARKASSE_ACCEPT,
                ErsteBankConstants.QUERYPARAMS.REDIRECT_URI_AUTHENTICATION)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .post(String.class, ErsteBankConstants.BODY.USERNAME + username);

        return ErsteBankCryptoUtil.getSidentityCode(html);
    }

    public TokenEntity getSidentityToken() throws LoginException {
        HttpResponse response = getRequest(ErsteBankConstants.URLS.LOGIN_BASE, ErsteBankConstants.URLS.OAUTH,
                ErsteBankConstants.QUERYPARAMS.SPARKASSE_ACCEPT,
                ErsteBankConstants.QUERYPARAMS.REDIRECT_URI_AUTHENTICATION)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .post(HttpResponse.class);

        return ErsteBankCryptoUtil.getTokenFromResponse(response);
    }

}
