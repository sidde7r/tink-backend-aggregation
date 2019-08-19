package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking;

import java.time.LocalDate;
import java.util.Collection;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.SebConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc.FetchPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc.PaymentSigningRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc.PaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.rpc.FetchCardAccountsTransactions;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SebApiClient extends SebBaseApiClient {

    public SebApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        super(client, sessionStorage);
    }

    @Override
    public RequestBuilder getAuthorizeUrl() {
        return client.request(new URL(SebConstants.Urls.BASE_AUTH_URL));
    }

    @Override
    public OAuth2Token getToken(TokenRequest request) {
        return client.request(
                        new URL(SebCommonConstants.Urls.BASE_URL)
                                .concat(SebCommonConstants.Urls.TOKEN))
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, request.toData())
                .toTinkToken();
    }

    public FetchAccountResponse fetchAccounts() {
        return createRequestInSession(
                        new URL(SebCommonConstants.Urls.BASE_URL)
                                .concat(SebConstants.Urls.ACCOUNTS))
                .queryParam(
                        SebCommonConstants.QueryKeys.WITH_BALANCE,
                        SebCommonConstants.QueryValues.WITH_BALANCE)
                .get(FetchAccountResponse.class);
    }

    public FetchTransactionsResponse fetchTransactions(
            String urlAddress, boolean appendQueryParams) {

        URL url = new URL(SebCommonConstants.Urls.BASE_URL).concat(urlAddress);

        RequestBuilder requestBuilder = createRequestInSession(url);

        if (appendQueryParams) {
            requestBuilder.queryParam(
                    SebCommonConstants.QueryKeys.BOOKING_STATUS,
                    SebCommonConstants.QueryValues.BOOKED_TRANSACTIONS);
        }

        FetchTransactionsResponse response = requestBuilder.get(FetchTransactionsResponse.class);

        return response;
    }

    public Collection<CreditCardAccount> fetchCardAccounts() {
        return createRequestInSession(
                        new URL(SebCommonConstants.Urls.BASE_URL)
                                .concat(SebConstants.Urls.CREDIT_CARD_ACCOUNTS))
                .get(FetchCardAccountResponse.class)
                .getTransactions();
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

    public CreatePaymentResponse createPaymentInitiation(
            CreatePaymentRequest createPaymentRequest, String paymentProduct) {
        return createRequestInSession(
                        new URL(SebCommonConstants.Urls.BASE_URL)
                                .concat(Urls.CREATE_PAYMENT)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct))
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    public FetchPaymentResponse getPayment(String paymentId, String paymentProduct) {
        return createRequestInSession(
                        new URL(SebCommonConstants.Urls.BASE_URL)
                                .concat(Urls.GET_PAYMENT)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct)
                                .parameter(IdTags.PAYMENT_ID, paymentId))
                .get(FetchPaymentResponse.class);
    }

    public PaymentStatusResponse getPaymentStatus(String paymentId, String paymentProduct) {
        return createRequestInSession(
                        new URL(SebCommonConstants.Urls.BASE_URL)
                                .concat(Urls.GET_PAYMENT_STATUS)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct)
                                .parameter(IdTags.PAYMENT_ID, paymentId))
                .get(PaymentStatusResponse.class);
    }

    public PaymentStatusResponse signPayment(
            String paymentId, String paymentProduct, PaymentSigningRequest body) {
        return createRequestInSession(
                        new URL(SebCommonConstants.Urls.BASE_URL)
                                .concat(Urls.SIGN_PAYMENT)
                                .parameter(IdTags.PAYMENT_PRODUCT, paymentProduct)
                                .parameter(IdTags.PAYMENT_ID, paymentId))
                .post(PaymentStatusResponse.class, body);
    }
}
