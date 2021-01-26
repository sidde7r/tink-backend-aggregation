package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking;

import java.time.LocalDate;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc.FetchPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc.PaymentSigningRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc.PaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.entities.TransactionDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transferdestinations.rpc.FetchAccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.AuthResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.AuthorizeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountsTransactions;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SebApiClient extends SebBaseApiClient {

    public SebApiClient(
            TinkHttpClient client, PersistentStorage persistentStorage, boolean isManualRequest) {
        super(client, persistentStorage, isManualRequest);
    }

    @Override
    public RequestBuilder getAuthorizeUrl() {
        return client.request(new URL(Urls.OAUTH));
    }

    @Override
    public AuthResponse initBankId() {
        final HttpResponse response =
                client.request(new URL(SebCommonConstants.Urls.INIT_BANKID))
                        .accept(MediaType.APPLICATION_JSON)
                        .post(HttpResponse.class);

        final String csrfToken = response.getHeaders().getFirst(HeaderKeys.X_SEB_CSRF);
        return response.getBody(AuthResponse.class).withCsrfToken(csrfToken);
    }

    @Override
    public AuthResponse collectBankId(final String csrfToken) {
        final HttpResponse response =
                client.request(new URL(SebCommonConstants.Urls.INIT_BANKID))
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HeaderKeys.X_SEB_CSRF, csrfToken)
                        .get(HttpResponse.class);

        final String newCsrfToken = response.getHeaders().getFirst(HeaderKeys.X_SEB_CSRF);
        return response.getBody(AuthResponse.class).withCsrfToken(newCsrfToken);
    }

    @Override
    public AuthorizeResponse getAuthorization(String clientId, String redirectUri) {
        return client.request(getAuthorizeUrl().getUrl())
                .accept(MediaType.APPLICATION_JSON)
                .queryParam(QueryKeys.CLIENT_ID, clientId)
                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.RESPONSE_TYPE_TOKEN)
                .queryParam(QueryKeys.SCOPE, QueryValues.SCOPE)
                .queryParam(QueryKeys.REDIRECT_URI, redirectUri)
                .get(AuthorizeResponse.class);
    }

    public AuthorizeResponse postAuthorization(final String requestForm) {
        return client.request(Urls.OAUTH)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .body(requestForm)
                .post(AuthorizeResponse.class);
    }

    @Override
    public OAuth2Token getToken(TokenRequest request) {
        return client.request(new URL(Urls.TOKEN))
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, request.toData())
                .toTinkToken();
    }

    public FetchAccountResponse fetchAccounts() {
        return createRequestInSession(new URL(Urls.BASE_URL).concat(SebConstants.Urls.ACCOUNTS))
                .queryParam(QueryKeys.WITH_BALANCE, QueryValues.WITH_BALANCE)
                .get(FetchAccountResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(
            String urlAddress, boolean appendQueryParams) {

        URL url = new URL(Urls.BASE_URL).concat(urlAddress);

        RequestBuilder requestBuilder = createRequestInSession(url);

        if (appendQueryParams) {
            requestBuilder.queryParam(
                    QueryKeys.BOOKING_STATUS, QueryValues.PENDING_AND_BOOKED_TRANSACTIONS);
        }

        return requestBuilder.get(FetchTransactionsResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(
            String accountId, LocalDate from, LocalDate to) {
        return createRequestInSession(
                        new URL(Urls.BASE_URL)
                                .concat(SebConstants.Urls.TRANSACTIONS)
                                .parameter(SebCommonConstants.IdTags.ACCOUNT_ID, accountId))
                .queryParam(QueryKeys.DATE_FROM, from.toString())
                .queryParam(QueryKeys.DATE_TO, to.toString())
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.PENDING_AND_BOOKED_TRANSACTIONS)
                .get(FetchTransactionsResponse.class);
    }

    public FetchTransactionsResponse fetchUpcomingTransactions(URL url) {
        return createRequestInSession(url)
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.PENDING)
                .get(FetchTransactionsResponse.class);
    }

    public TransactionDetailsEntity fetchTransactionDetails(String urlAddress) {
        return createRequestInSession(
                        new URL(SebCommonConstants.Urls.BASE_URL)
                                .concat(SebConstants.Urls.BASE_AIS)
                                .concat(urlAddress))
                .get(TransactionDetailsEntity.class);
    }

    public FetchCardAccountResponse fetchCardAccounts() {
        return createRequestInSession(
                        new URL(SebCommonConstants.Urls.BASE_URL)
                                .concat(SebConstants.Urls.CREDIT_CARD_ACCOUNTS))
                .get(FetchCardAccountResponse.class);
    }

    @Override
    public FetchCardAccountsTransactions fetchCardTransactions(
            String accountId, LocalDate fromDate, LocalDate toDate) {

        URL url =
                new URL(SebCommonConstants.Urls.BASE_URL)
                        .concat(SebConstants.Urls.CREDIT_CARD_TRANSACTIONS)
                        .parameter(SebCommonConstants.IdTags.ACCOUNT_ID, accountId);

        return buildCardTransactionsFetch(url, fromDate, toDate)
                .get(FetchCardAccountsTransactions.class);
    }

    public FetchAccountDetailsResponse fetchAccountDetails(String accountId) {
        return createRequestInSession(
                        new URL(SebCommonConstants.Urls.BASE_URL)
                                .concat(SebConstants.Urls.ACCOUNT_DETAILS)
                                .parameter(SebCommonConstants.IdTags.ACCOUNT_ID, accountId))
                .get(FetchAccountDetailsResponse.class);
    }

    public CreatePaymentResponse createPaymentInitiation(
            CreatePaymentRequest createPaymentRequest, String paymentProduct) {
        return createRequestInSession(
                        new URL(SebCommonConstants.Urls.BASE_URL)
                                .concat(SebConstants.Urls.CREATE_PAYMENT)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct))
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    public FetchPaymentResponse getPayment(String paymentId, String paymentProduct) {
        return createRequestInSession(
                        new URL(SebCommonConstants.Urls.BASE_URL)
                                .concat(SebConstants.Urls.GET_PAYMENT)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct)
                                .parameter(IdTags.PAYMENT_ID, paymentId))
                .get(FetchPaymentResponse.class);
    }

    public PaymentStatusResponse getPaymentStatus(String paymentId, String paymentProduct)
            throws PaymentException {
        return createRequestInSession(
                        new URL(SebCommonConstants.Urls.BASE_URL)
                                .concat(SebConstants.Urls.GET_PAYMENT_STATUS)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct)
                                .parameter(IdTags.PAYMENT_ID, paymentId))
                .get(PaymentStatusResponse.class)
                .checkForErrors();
    }

    public PaymentStatusResponse signPayment(
            String paymentId, String paymentProduct, PaymentSigningRequest body) {
        return createRequestInSession(
                        new URL(SebCommonConstants.Urls.BASE_URL)
                                .concat(SebConstants.Urls.SIGN_PAYMENT)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct)
                                .parameter(IdTags.PAYMENT_ID, paymentId))
                .post(PaymentStatusResponse.class, body);
    }
}
