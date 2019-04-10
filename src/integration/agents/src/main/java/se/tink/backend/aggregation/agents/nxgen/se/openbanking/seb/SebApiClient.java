package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb;

import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SebConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.configuration.SebConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class SebApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private SebConfiguration configuration;

    public SebApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public SebConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(SebConfiguration configuration) {
        this.configuration = configuration;
    }

    public FetchAccountResponse fetchAccounts() {
        final String baseUrl = getConfiguration().getBaseUrl();

        return client.request(baseUrl + SebConstants.Urls.ACCOUNTS)
                .accept(MediaType.APPLICATION_JSON)
                .header(SebConstants.HeaderKeys.X_REQUEST_ID, getRequestId())
                .addBearerToken(getTokenFromSession())
                .queryParam(
                        SebConstants.QueryKeys.WITH_BALANCE, SebConstants.QueryValues.WITH_BALANCE)
                .get(FetchAccountResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(TransactionalAccount account, int page) {
        final String baseUrl = getConfiguration().getBaseUrl();
        final URL url =
                new URL(baseUrl + SebConstants.Urls.TRANSACTIONS)
                        .parameter(
                                SebConstants.IdTags.ACCOUNT_ID,
                                account.getFromTemporaryStorage(
                                        SebConstants.StorageKeys.ACCOUNT_ID));

        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .header(SebConstants.HeaderKeys.X_REQUEST_ID, getRequestId())
                .addBearerToken(getTokenFromSession())
                .queryParam(
                        SebConstants.QueryKeys.TRANSACTION_SEQUENCE_NUMBER, Integer.toString(page))
                .queryParam(
                        SebConstants.QueryKeys.BOOKING_STATUS,
                        SebConstants.QueryValues.BOOKED_TRANSACTIONS)
                .get(FetchTransactionsResponse.class);
    }

    public URL getAuthorizeUrl(String state) {
        final String clientId = getConfiguration().getClientId();

        final String redirectUri = getConfiguration().getRedirectUrl();
        final String baseUrl = getConfiguration().getBaseUrl();

        return createRequestInSession(baseUrl + SebConstants.Urls.OAUTH)
                .queryParam(SebConstants.QueryKeys.CLIENT_ID, clientId)
                .queryParam(
                        SebConstants.QueryKeys.RESPONSE_TYPE,
                        SebConstants.QueryValues.RESPONSE_TYPE_TOKEN)
                .queryParam(SebConstants.QueryKeys.SCOPE, SebConstants.QueryValues.SCOPE)
                .queryParam(SebConstants.QueryKeys.REDIRECT_URI, redirectUri)
                .queryParam(SebConstants.QueryKeys.STATE, state)
                .getUrl();
    }

    public OAuth2Token getToken(String code) {
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();

        final String redirectUri = getConfiguration().getRedirectUrl();
        final String baseUrl = getConfiguration().getBaseUrl();

        TokenRequest request =
                new TokenRequest(
                        clientId,
                        clientSecret,
                        redirectUri,
                        code,
                        SebConstants.QueryValues.GRANT_TYPE,
                        SebConstants.QueryValues.SCOPE);

        return client.request(baseUrl + SebConstants.Urls.TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponse.class, request.toData())
                .toTinkToken();
    }

    public OAuth2Token refreshToken(String refreshToken) throws SessionException {
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();

        final String redirectUri = getConfiguration().getRedirectUrl();
        final String baseUrl = getConfiguration().getBaseUrl();

        try {
            RefreshRequest request =
                    new RefreshRequest(refreshToken, clientId, clientSecret, redirectUri);

            return client.request(baseUrl + SebConstants.Urls.TOKEN)
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

    public void setTokenToSession(OAuth2Token token) {
        sessionStorage.put(SebConstants.StorageKeys.TOKEN, token);
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url).accept(MediaType.TEXT_HTML);
    }

    private RequestBuilder createRequestInSession(URL url) {
        return createRequest(url);
    }

    private RequestBuilder createRequestInSession(String url) {
        return createRequestInSession(new URL(url));
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(SebConstants.StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }

    private String getRequestId() {
        return java.util.UUID.randomUUID().toString();
    }
}
