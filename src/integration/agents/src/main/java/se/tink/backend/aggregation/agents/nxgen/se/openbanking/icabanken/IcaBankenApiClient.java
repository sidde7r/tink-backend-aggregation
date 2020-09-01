package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken;

import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.Account;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.ProductionUrls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.SandboxUrls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.AuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.configuration.IcaBankenConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.payment.enums.PaymentType;

public final class IcaBankenApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;

    private IcaBankenConfiguration configuration;
    private FetchAccountsResponse cachedAccounts;

    public IcaBankenApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    public void setConfiguration(
            IcaBankenConfiguration icaBankenConfiguration,
            EidasProxyConfiguration eidasProxyConfiguration) {
        this.configuration = icaBankenConfiguration;
        client.setEidasProxy(eidasProxyConfiguration);
    }

    public IcaBankenConfiguration getConfiguration() {
        return configuration;
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    public TokenResponse exchangeAuthorizationCode(AuthorizationRequest request) {
        return client.request(new URL(ProductionUrls.TOKEN_PATH))
                .body(request, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class);
    }

    public TokenResponse exchangeRefreshToken(RefreshTokenRequest request) {
        return client.request(new URL(ProductionUrls.TOKEN_PATH))
                .body(request, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class);
    }

    public FetchAccountsResponse fetchAccounts() {
        if (cachedAccounts == null) {
            cachedAccounts =
                    client.request(new URL(ProductionUrls.ACCOUNTS_PATH))
                            .queryParam(QueryKeys.WITH_BALANCE, QueryValues.WITH_BALANCE)
                            .header(
                                    HeaderKeys.AUTHORIZATION,
                                    HeaderValues.BEARER + persistentStorage.get(StorageKeys.TOKEN))
                            .header(HeaderKeys.SCOPE, HeaderValues.ACCOUNT)
                            .header(Psd2Headers.Keys.X_REQUEST_ID, Psd2Headers.getRequestId())
                            .get(FetchAccountsResponse.class);
        }

        return cachedAccounts;
    }

    public FetchTransactionsResponse fetchTransactionsForAccount(
            String apiIdentifier, Date fromDate, Date toDate) {
        final URL baseUrl = new URL(ProductionUrls.TRANSACTIONS_PATH);
        final URL requestUrl = baseUrl.parameter(Account.ACCOUNT_ID, apiIdentifier);

        return client.request(requestUrl)
                .queryParam(
                        QueryKeys.DATE_FROM, ThreadSafeDateFormat.FORMATTER_DAILY.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, ThreadSafeDateFormat.FORMATTER_DAILY.format(toDate))
                .queryParam(QueryKeys.STATUS, QueryValues.STATUS)
                .header(Psd2Headers.Keys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(
                        HeaderKeys.AUTHORIZATION,
                        HeaderValues.BEARER + persistentStorage.get(StorageKeys.TOKEN))
                .header(HeaderKeys.SCOPE, HeaderValues.ACCOUNT)
                .get(FetchTransactionsResponse.class);
    }

    public GetPaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, PaymentType paymentType) {
        URL uri = new URL(SandboxUrls.INITIATE_PAYMENT);
        return createRequest(
                        uri.parameter(QueryKeys.PAYMENT_PRODUCT, paymentTypeToString(paymentType)))
                .header(Psd2Headers.Keys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .addBearerToken(getApplicationTokenFromSession())
                .post(GetPaymentResponse.class, createPaymentRequest);
    }

    public GetPaymentResponse getPayment(String uniqueId, PaymentType paymentType) {

        URL uri = new URL(SandboxUrls.GET_PAYMENT);
        return createRequest(
                        uri.parameter(
                                        IcaBankenConstants.QueryKeys.PAYMENT_PRODUCT,
                                        paymentTypeToString(paymentType))
                                .parameter(IcaBankenConstants.QueryKeys.PAYMENT_ID, uniqueId))
                .header(Psd2Headers.Keys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .addBearerToken(getApplicationTokenFromSession())
                .get(GetPaymentResponse.class);
    }

    private String paymentTypeToString(PaymentType paymentType) {
        return paymentType.equals(PaymentType.INTERNATIONAL)
                ? QueryValues.PaymentProduct.INTERNATIONAL
                : QueryValues.PaymentProduct.SEPA;
    }

    private OAuth2Token getApplicationTokenFromSession() {
        return persistentStorage
                .get(StorageKeys.TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_TOKEN));
    }
}
