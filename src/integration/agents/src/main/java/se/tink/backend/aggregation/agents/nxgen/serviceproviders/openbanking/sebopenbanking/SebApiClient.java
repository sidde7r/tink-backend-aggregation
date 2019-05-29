package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.SebConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.configuration.SebConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.rpc.FetchCardAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.rpc.FetchCardAccountsTransactions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
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

    public void setConfiguration(SebConfiguration configuration) {
        this.configuration = configuration;
    }

    public SebConfiguration getConfiguration() {
        return configuration;
    }

    public FetchAccountResponse fetchAccounts() {

        URL url = new URL(configuration.getBaseUrl() + SebConstants.Urls.ACCOUNTS);

        FetchAccountResponse response =
                client.request(url)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(SebConstants.HeaderKeys.X_REQUEST_ID, getRequestId())
                        .addBearerToken(getTokenFromSession())
                        .queryParam(
                                SebConstants.QueryKeys.WITH_BALANCE,
                                SebConstants.QueryValues.WITH_BALANCE)
                        .get(FetchAccountResponse.class);

        return response;
    }

    public FetchTransactionsResponse fetchTransactions(
            String urlAddress, boolean appendQueryParams) {

        URL url = new URL(configuration.getBaseUrl() + urlAddress);

        RequestBuilder requestBuilder =
                client.request(url)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HeaderKeys.X_REQUEST_ID, getRequestId())
                        .addBearerToken(getTokenFromSession());

        if (appendQueryParams) {
            requestBuilder.queryParam(
                    SebConstants.QueryKeys.BOOKING_STATUS,
                    SebConstants.QueryValues.BOOKED_TRANSACTIONS);
        }

        FetchTransactionsResponse response = requestBuilder.get(FetchTransactionsResponse.class);

        return response;
    }

    public FetchTransactionsResponse fetchTransactions(TransactionalAccount account) {

        String url =
                new URL(SebConstants.Urls.TRANSACTIONS)
                        .parameter(SebConstants.IdTags.ACCOUNT_ID, account.getApiIdentifier())
                        .toString();

        return fetchTransactions(url, true);
    }

    public URL getAuthorizeUrl(String state) {
        return createRequestInSession(new URL(configuration.getBaseUrl() + SebConstants.Urls.OAUTH))
                .queryParam(SebConstants.QueryKeys.CLIENT_ID, configuration.getClientId())
                .queryParam(
                        SebConstants.QueryKeys.RESPONSE_TYPE,
                        SebConstants.QueryValues.RESPONSE_TYPE_TOKEN)
                .queryParam(SebConstants.QueryKeys.SCOPE, SebConstants.QueryValues.SCOPE)
                .queryParam(SebConstants.QueryKeys.REDIRECT_URI, configuration.getRedirectUrl())
                .queryParam(SebConstants.QueryKeys.STATE, state)
                .getUrl();
    }

    public OAuth2Token getToken(String code) {
        TokenRequest request =
                new TokenRequest(
                        configuration.getClientId(),
                        configuration.getClientSecret(),
                        configuration.getRedirectUrl(),
                        code,
                        SebConstants.QueryValues.GRANT_TYPE,
                        SebConstants.QueryValues.SCOPE);

        OAuth2Token token =
                client.request(new URL(configuration.getBaseUrl() + SebConstants.Urls.TOKEN))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                        .accept(MediaType.APPLICATION_JSON)
                        .post(TokenResponse.class, request.toData())
                        .toTinkToken();

        return token;
    }

    public OAuth2Token refreshToken(String refreshToken) throws SessionException {
        try {
            RefreshRequest request =
                    new RefreshRequest(
                            refreshToken,
                            configuration.getClientId(),
                            configuration.getClientSecret(),
                            configuration.getRedirectUrl());

            return client.request(new URL(configuration.getBaseUrl() + SebConstants.Urls.TOKEN))
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

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(SebConstants.StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }

    private String getRequestId() {
        return java.util.UUID.randomUUID().toString();
    }

    public Collection<CreditCardAccount> fetchCreditAccounts() {
        URL url = new URL(configuration.getBaseUrl() + SebConstants.Urls.CREDIT_CARD_ACCOUNTS);

        FetchCardAccountResponse response =
                client.request(url)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(SebConstants.HeaderKeys.X_REQUEST_ID, getRequestId())
                        .addBearerToken(getTokenFromSession())
                        .get(FetchCardAccountResponse.class);

        return response.getTransactions();
    }

    public FetchCardAccountsTransactions fetchTransactions(
            String accountId, LocalDate fromDate, LocalDate toDate) {

        URL url =
                new URL(configuration.getBaseUrl() + SebConstants.Urls.CREDIT_CARD_TRANSACTIONS)
                        .parameter(SebConstants.IdTags.ACCOUNT_ID, accountId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SebConstants.DATE_FORMAT);

        FetchCardAccountsTransactions response =
                client.request(url)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(SebConstants.HeaderKeys.X_REQUEST_ID, getRequestId())
                        .addBearerToken(getTokenFromSession())
                        .queryParam(SebConstants.QueryKeys.DATE_FROM, fromDate.format(formatter))
                        .queryParam(SebConstants.QueryKeys.DATE_TO, toDate.format(formatter))
                        .get(FetchCardAccountsTransactions.class);

        return response;
    }
}
