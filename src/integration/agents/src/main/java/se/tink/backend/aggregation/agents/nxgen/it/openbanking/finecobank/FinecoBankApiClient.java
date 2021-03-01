package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ParameterKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentAuthorizationsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.authenticator.rpc.PostConsentBodyRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.card.rpc.CardAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.card.rpc.CardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.enums.FinecoBankPaymentProduct;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.GetPaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class FinecoBankApiClient {

    private final TinkHttpClient client;
    private final FinecoHeaderValues headerValues;
    private final RandomValueGenerator randomValueGenerator;

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, randomValueGenerator.getUUID())
                .header(HeaderKeys.PSU_IP_ADDRESS, headerValues.getUserIp());
    }

    public ConsentResponse getConsent(PostConsentBodyRequest postConsentBodyRequest, String state) {
        return createRequest(Urls.CONSENTS)
                .header(
                        HeaderKeys.TPP_REDIRECT_URI,
                        (new URL(headerValues.getRedirectUrl()).queryParam(QueryKeys.STATE, state)))
                .body(postConsentBodyRequest)
                .post(ConsentResponse.class);
    }

    public ConsentStatusResponse getConsentStatus(String consentId) {
        return createRequest(Urls.CONSENT_STATUS.parameter(ParameterKeys.CONSENT_ID, consentId))
                .get(ConsentStatusResponse.class);
    }

    public CardAccountsResponse fetchCreditCardAccounts(String consentId) {
        return createRequest(Urls.CARD_ACCOUNTS)
                .header(HeaderKeys.CONSENT_ID, consentId)
                .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                .get(CardAccountsResponse.class);
    }

    public AccountsResponse fetchAccounts(String consentId) {
        return createRequest(Urls.ACCOUNTS)
                .header(HeaderKeys.CONSENT_ID, consentId)
                .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                .get(AccountsResponse.class);
    }

    public PaginatorResponse getTransactions(
            String consentId, TransactionalAccount account, Date fromDate, Date toDate) {
        SimpleDateFormat paginationDateFormatter =
                new SimpleDateFormat(Formats.DEFAULT_DATE_FORMAT);

        return createRequest(
                        Urls.TRANSACTIONS.parameter(
                                ParameterKeys.ACCOUNT_ID, account.getApiIdentifier()))
                .header(HeaderKeys.CONSENT_ID, consentId)
                .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKED)
                .queryParam(QueryKeys.DATE_FROM, paginationDateFormatter.format(fromDate))
                .queryParam(QueryKeys.DATE_TO, paginationDateFormatter.format(toDate))
                .get(TransactionsResponse.class);
    }

    public PaginatorResponse getCreditTransactions(
            String consentId, CreditCardAccount account, LocalDate fromDate) {
        DateTimeFormatter paginationDateFormatter =
                DateTimeFormatter.ofPattern(Formats.DEFAULT_DATE_FORMAT);

        return createRequest(
                        Urls.CARD_TRANSACTIONS.parameter(
                                ParameterKeys.ACCOUNT_ID, account.getApiIdentifier()))
                .header(HeaderKeys.CONSENT_ID, consentId)
                .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKED)
                .queryParam(QueryKeys.DATE_FROM, paginationDateFormatter.format(fromDate))
                .get(CardTransactionsResponse.class);
    }

    public ConsentAuthorizationsResponse getConsentAuthorizations(String consentId) {
        return createRequest(
                        Urls.CONSENT_AUTHORIZATIONS.parameter(ParameterKeys.CONSENT_ID, consentId))
                .get(ConsentAuthorizationsResponse.class);
    }

    public CreatePaymentResponse createPayment(
            FinecoBankPaymentProduct paymentProduct,
            String state,
            CreatePaymentRequest requestBody) {
        final URL tppRedirectUrl =
                new URL(headerValues.getRedirectUrl()).queryParam(QueryKeys.STATE, state);

        return createRequest(
                        Urls.PAYMENT_INITIATION.parameter(
                                ParameterKeys.PAYMENT_PRODUCT, paymentProduct.getValue()))
                .header(HeaderKeys.TPP_REDIRECT_URI, tppRedirectUrl.toString())
                .post(CreatePaymentResponse.class, requestBody);
    }

    public GetPaymentResponse getPayment(
            FinecoBankPaymentProduct paymentProduct, String paymentId) {
        return createRequest(
                        Urls.GET_PAYMENT
                                .parameter(ParameterKeys.PAYMENT_PRODUCT, paymentProduct.getValue())
                                .parameter(ParameterKeys.PAYMENT_ID, paymentId))
                .get(GetPaymentResponse.class);
    }

    public GetPaymentStatusResponse getPaymentStatus(
            FinecoBankPaymentProduct paymentProduct, String paymentId) {
        return createRequest(
                        Urls.GET_PAYMENT_STATUS
                                .parameter(ParameterKeys.PAYMENT_PRODUCT, paymentProduct.getValue())
                                .parameter(ParameterKeys.PAYMENT_ID, paymentId))
                .get(GetPaymentStatusResponse.class);
    }
}
