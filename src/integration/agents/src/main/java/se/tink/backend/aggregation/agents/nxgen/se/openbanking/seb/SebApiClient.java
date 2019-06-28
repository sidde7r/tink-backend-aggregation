package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb;

import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SebConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SebConstants.Format;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SebConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SebConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SebConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SebConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SebConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.configuration.SebConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.creditcardaccount.rpc.CreditCardAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.creditcardaccount.rpc.CreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.utils.DateUtils;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
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
        return client.request(Urls.ACCOUNTS)
                .accept(MediaType.APPLICATION_JSON)
                .addBearerToken(getTokenFromSession())
                .header(HeaderKeys.X_REQUEST_ID, getRequestId())
                .header(HeaderKeys.PSU_CORPORATE_ID, HeaderValues.PSU_CORPORATE_ID)
                .queryParam(QueryKeys.WITH_BALANCE, QueryValues.WITH_BALANCE)
                .get(FetchAccountResponse.class);
    }

    public CreditCardAccountsResponse fetchCreditCardAccounts() {
        return client.request(Urls.BASE_URL + Urls.BRANDED_ACCOUNTS)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, getRequestId())
                .header(HeaderKeys.PSU_CORPORATE_ID, HeaderValues.PSU_CORPORATE_ID)
                .addBearerToken(getTokenFromSession())
                .get(CreditCardAccountsResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(
            TransactionalAccount account, Date fromDate, Date toDate) {
        final URL url =
                new URL(SebConstants.Urls.TRANSACTIONS)
                        .parameter(
                                SebConstants.IdTags.ACCOUNT_ID,
                                account.getFromTemporaryStorage(
                                        SebConstants.StorageKeys.ACCOUNT_ID));

        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, getRequestId())
                .header(HeaderKeys.PSU_CORPORATE_ID, HeaderValues.PSU_CORPORATE_ID)
                .addBearerToken(getTokenFromSession())
                .queryParam(
                        QueryKeys.DATE_FROM,
                        DateUtils.formatDateTime(fromDate, Format.TIMESTAMP, Format.TIMEZONE))
                .queryParam(
                        QueryKeys.DATE_TO,
                        DateUtils.formatDateTime(toDate, Format.TIMESTAMP, Format.TIMEZONE))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKED_TRANSACTIONS)
                .get(FetchTransactionsResponse.class);
    }

    public CreditCardTransactionsResponse fetCreditCardTransactions(
            CreditCardAccount account, Date fromDate, Date toDate) {

        final URL url =
                new URL(Urls.BASE_URL + Urls.BRANDED_ACCOUNTS + Urls.BRANDED_TRANSACTIONS)
                        .parameter(
                                SebConstants.IdTags.ACCOUNT_ID,
                                account.getFromTemporaryStorage(
                                        SebConstants.StorageKeys.ACCOUNT_ID));

        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, getRequestId())
                .header(HeaderKeys.PSU_CORPORATE_ID, HeaderValues.PSU_CORPORATE_ID)
                .addBearerToken(getTokenFromSession())
                .queryParam(
                        QueryKeys.DATE_TO,
                        DateUtils.formatDateTime(toDate, Format.TIMESTAMP, Format.TIMEZONE))
                .queryParam(
                        QueryKeys.DATE_FROM,
                        DateUtils.formatDateTime(fromDate, Format.TIMESTAMP, Format.TIMEZONE))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKED_TRANSACTIONS)
                .get(CreditCardTransactionsResponse.class);
    }

    public URL getAuthorizeUrl(String state) {
        final String clientId = getConfiguration().getClientId();
        final String redirectUri = getConfiguration().getRedirectUrl();

        return createRequestInSession(Urls.OAUTH)
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

        TokenRequest request =
                new TokenRequest(
                        clientId,
                        clientSecret,
                        redirectUri,
                        code,
                        SebConstants.QueryValues.GRANT_TYPE,
                        SebConstants.QueryValues.SCOPE);

        return client.request(Urls.TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponse.class, request.toData())
                .toTinkToken();
    }

    public OAuth2Token refreshToken(String refreshToken) throws SessionException {
        final String clientId = getConfiguration().getClientId();
        final String clientSecret = getConfiguration().getClientSecret();
        final String redirectUri = getConfiguration().getRedirectUrl();

        try {
            RefreshRequest request =
                    new RefreshRequest(refreshToken, clientId, clientSecret, redirectUri);

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
