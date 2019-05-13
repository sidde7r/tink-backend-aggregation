package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard;

import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardConstants.Format;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.authenticator.rpc.AuthorizationCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.configuration.EnterCardConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.fetcher.rpc.CreditCardAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.fetcher.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.utils.DateUtils;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.utils.EnterCardUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import tink.org.apache.http.HttpHeaders;

public final class EnterCardApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private EnterCardConfiguration configuration;

    public EnterCardApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    private EnterCardConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(EnterCardConfiguration configuration) {
        this.configuration = configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromStorage();
        final String clientId = configuration.getClientId();

        return createRequest(url).header(HeaderKeys.CLIENT_ID, clientId).addBearerToken(authToken);
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public CreditCardAccountResponse fetchCreditCardAccounts() {
        return createRequestInSession(Urls.ACCOUNTS).get(CreditCardAccountResponse.class);
    }

    public PaginatorResponse fetchCreditCardTransactions(
            CreditCardAccount account, Date fromDate, Date toDate) {

        return createRequestInSession(Urls.TRANSACTIONS)
                .queryParam(
                        QueryKeys.FROM_BOOKING_DATE_TIME,
                        DateUtils.formatDateTime(fromDate, Format.TIMESTAMP, Format.TIMEZONE))
                .queryParam(
                        QueryKeys.TO_BOOKING_DATE_TIME,
                        DateUtils.formatDateTime(toDate, Format.TIMESTAMP, Format.TIMEZONE))
                .queryParam(QueryKeys.ACCOUNT_NUMBER, account.getAccountNumber())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .get(CreditCardTransactionsResponse.class);
    }

    public OAuth2Token getAuth() {
        final String clientId = getConfiguration().getClientId();

        AuthorizationCodeResponse response =
                client.request(Urls.AUTHORIZATION)
                        .accept(MediaType.APPLICATION_JSON)
                        .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                        .queryParam(QueryKeys.CLIENT_ID, clientId)
                        .get(AuthorizationCodeResponse.class);

        persistentStorage.put(StorageKeys.AUTH_CODE, response.getCode());
        setTokenToStorage(getToken(response.getCode()));

        return getToken(response.getCode());
    }

    public URL getAuthorizeUrl(String state) {
        final String clientId = getConfiguration().getClientId();
        final String redirectUri = getConfiguration().getRedirectUrl();
        final String codeVerifier = EnterCardUtils.getCodeVerifier();
        persistentStorage.put(StorageKeys.CODE_VERIFIER, codeVerifier);

        return createRequest(Urls.AUTHORIZATION)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                .queryParam(QueryKeys.STATE, state)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.CLIENT_ID, clientId)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .queryParam(QueryKeys.CODE_CHALLENGE, EnterCardUtils.getCodeChallenge(codeVerifier))
                .queryParam(QueryKeys.CODE_CHALLENGE_METHOD, QueryValues.S256)
                .getUrl();
    }

    public OAuth2Token getToken(String code) {
        final String clientId = getConfiguration().getClientId();
        final String redirectUri = getConfiguration().getRedirectUrl();
        final String codeVerifier = EnterCardUtils.getCodeVerifier();
        final String clientSecret = getConfiguration().getClientSecret();

        TokenRequest request =
                new TokenRequest(
                        QueryValues.SCOPE,
                        code,
                        QueryValues.GRANT_TYPE,
                        clientId,
                        clientSecret,
                        redirectUri,
                        codeVerifier);

        return client.request(Urls.TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponse.class, request.toData())
                .toTinkToken();
    }

    public OAuth2Token refreshToken(String refreshToken) throws SessionException {
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();

        try {
            RefreshTokenRequest request =
                    new RefreshTokenRequest(
                            QueryValues.SCOPE,
                            QueryValues.GRANT_TYPE,
                            refreshToken,
                            clientId,
                            clientSecret);

            return client.request(Urls.TOKEN)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .post(TokenResponse.class, request.toData())
                    .toTinkToken();

        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 401) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
            throw e;
        }
    }

    public void setTokenToStorage(OAuth2Token token) {
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, token);
    }
}
