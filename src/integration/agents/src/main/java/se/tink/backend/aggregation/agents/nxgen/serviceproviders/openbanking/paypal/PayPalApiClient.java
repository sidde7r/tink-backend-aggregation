package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpHeaders;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.authenticator.rpc.ClientCredentialsTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.configuration.PayPalConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class PayPalApiClient {
    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private PayPalConfiguration configuration;
    private String redirectUrl;

    public PayPalApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    private PayPalConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    private String getRedirectUrl() {
        return Optional.ofNullable(redirectUrl)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(AgentConfiguration<PayPalConfiguration> agentConfiguration) {
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromStorage();

        return createRequest(url).addBearerToken(authToken);
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }

    public OAuth2Token getToken(String code) {
        TokenRequest tokenRequest = new TokenRequest(FormValues.GRANT_TYPE, code);

        return client.request(Urls.TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .addBasicAuth(
                        getConfiguration().getClientId(), getConfiguration().getClientSecret())
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponse.class, tokenRequest.toData())
                .toTinkToken();
    }

    public OAuth2Token getClientCredentialsToken() {
        ClientCredentialsTokenRequest tokenRequest =
                new ClientCredentialsTokenRequest(PayPalConstants.FormValues.CLIENT_CREDENTIALS);

        return client.request(Urls.TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .addBasicAuth(
                        getConfiguration().getClientId(), getConfiguration().getClientSecret())
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponse.class, tokenRequest.toData())
                .toTinkToken();
    }

    public URL getAuthorizeUrl(String state) {
        return createRequest(Urls.AUTHORIZE)
                .queryParam(QueryKeys.CLIENT_ID, getConfiguration().getClientId())
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                .queryParam(QueryKeys.REDIRECT_URI, getRedirectUrl())
                .queryParam(QueryKeys.STATE, state)
                .getUrl();
    }

    public OAuth2Token refreshToken(String refreshToken) {
        RefreshTokenRequest refreshTokenRequest =
                new RefreshTokenRequest(FormValues.GRANT_TYPE_REFRESH, refreshToken);

        return createRequest(Urls.TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .addBasicAuth(
                        getConfiguration().getClientId(), getConfiguration().getClientSecret())
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponse.class, refreshTokenRequest.toData())
                .toTinkToken();
    }

    public FetchAccountResponse fetchAccount() {
        return createRequestInSession(Urls.IDENTITY)
                .queryParam(QueryKeys.SCHEMA, QueryValues.SCHEMA)
                .get(FetchAccountResponse.class);
    }

    public AccountBalanceResponse getAccountBalance() {
        return createRequestInSession(Urls.BALANCE).get(AccountBalanceResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> getTransactions() {
        return createRequestInSession(Urls.TRANSACTIONS)
                .queryParam(QueryKeys.START_TIME, getTransactionFetchStartTime())
                .queryParam(QueryKeys.END_TIME, getTransactionFetchEndTime())
                .get(FetchTransactionsResponse.class);
    }

    public TransactionKeyPaginatorResponse<String> getTransactionsForKey(String key) {
        return createRequestInSession(Urls.TRANSACTIONS)
                .queryParam(QueryKeys.START_TIME, getTransactionFetchStartTime())
                .queryParam(QueryKeys.END_TIME, getTransactionFetchEndTime())
                .queryParam(QueryKeys.NEXT_PAGE_TOKEN, key)
                .get(FetchTransactionsResponse.class);
    }

    private String getTransactionFetchEndTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return new SimpleDateFormat(PayPalConstants.Formats.TRANSACTION_DATE_FORMAT)
                .format(calendar.getTime());
    }

    private String getTransactionFetchStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.YEAR, -1);
        return new SimpleDateFormat(PayPalConstants.Formats.TRANSACTION_DATE_FORMAT)
                .format(calendar.getTime());
    }
}
