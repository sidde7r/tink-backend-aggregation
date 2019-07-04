package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab;

import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SBABConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SBABConstants.Format;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SBABConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SBABConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SBABConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SBABConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SBABConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SBABConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SBABConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.rpc.AuthorizationCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.configuration.SBABConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.rpc.FetchCustomerResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.util.Utils;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class SBABApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private SBABConfiguration configuration;

    public SBABApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    private SBABConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(SBABConfiguration configuration) {
        this.configuration = configuration;
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

    public URL getAuthorizeUrl(String state) {
        final String clientId = getConfiguration().getClientId();
        final String redirectUri = getConfiguration().getRedirectUrl();

        return createRequest(Urls.AUTHORIZATION)
                .queryParam(QueryKeys.CLIENT_ID, clientId)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                .getUrl();
    }

    public String getPendingAuthorizationCode() {
        return client.request(Urls.AUTHORIZATION)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                .queryParam(QueryKeys.USER_ID, QueryValues.TEST_USER)
                .accept(MediaType.APPLICATION_JSON)
                .header(
                        HeaderKeys.CLIENT_CERTIFICATE,
                        Utils.readFile(configuration.getClientCertificatePath()))
                .header(HeaderKeys.PSU_IP_ADDRESS, HeaderValues.PSU_IP_ADDRESS)
                .get(AuthorizationCodeResponse.class)
                .getPendingAuthorizationCode();
    }

    public OAuth2Token getToken(String code) {
        final String redirectUri = getConfiguration().getRedirectUrl();

        TokenRequest request = new TokenRequest(redirectUri, code, QueryValues.GRANT_TYPE);

        return client.request(Urls.TOKEN)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .header(
                        HeaderKeys.CLIENT_CERTIFICATE,
                        Utils.readFile(configuration.getClientCertificatePath()))
                .post(TokenResponse.class, request.toData())
                .toTinkToken();
    }

    public OAuth2Token refreshToken(String refreshToken) throws SessionException {
        final String redirectUri = getConfiguration().getRedirectUrl();

        try {
            RefreshTokenRequest request =
                    new RefreshTokenRequest(redirectUri, QueryValues.REFRESH_TOKEN, refreshToken);

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

    public FetchAccountResponse fetchAccounts() {
        return client.request(Urls.ACCOUNTS)
                .accept(MediaType.APPLICATION_JSON)
                .addBearerToken(getTokenFromStorage())
                .get(FetchAccountResponse.class);
    }

    public FetchCustomerResponse fetchCustomer() {
        return client.request(Urls.CUSTOMERS)
                .accept(MediaType.APPLICATION_JSON)
                .addBearerToken(getTokenFromStorage())
                .get(FetchCustomerResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(
            TransactionalAccount account, Date startDate, Date endDate) {
        return client.request(
                        Urls.TRANSACTIONS.parameter(
                                IdTags.ACCOUNT_NUMBER,
                                account.getFromTemporaryStorage(StorageKeys.ACCOUNT_NUMBER)))
                .queryParam(
                        QueryKeys.END_DATE,
                        Utils.formatDateTime(endDate, Format.TIMESTAMP, Format.TIMEZONE))
                .queryParam(
                        QueryKeys.START_DATE,
                        Utils.formatDateTime(startDate, Format.TIMESTAMP, Format.TIMEZONE))
                .accept(MediaType.APPLICATION_JSON)
                .addBearerToken(getTokenFromStorage())
                .get(FetchTransactionsResponse.class);
    }

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, String debtorAccountNumber) {
        return createRequest(
                        SBABConstants.Urls.INITIATE_PAYMENT.parameter(
                                IdTags.ACCOUNT_NUMBER, debtorAccountNumber))
                .addBearerToken(getTokenFromStorage())
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    public GetPaymentResponse getPayment(String transferId, String debtorId) {
        return createRequest(
                        SBABConstants.Urls.GET_PAYMENT
                                .parameter(IdTags.ACCOUNT_NUMBER, debtorId)
                                .parameter(IdTags.PAYMENT_ID, transferId))
                .addBearerToken(getTokenFromStorage())
                .get(GetPaymentResponse.class);
    }

    public String signPayment(String paymentId) {
        return createRequest(Urls.SIGN_PAYMENT.parameter(IdTags.PAYMENT_ID, paymentId))
                .addBearerToken(getTokenFromStorage())
                .put(String.class);
    }

    public void setTokenToSession(OAuth2Token token) {
        persistentStorage.put(SBABConstants.StorageKeys.OAUTH_TOKEN, token);
    }
}
