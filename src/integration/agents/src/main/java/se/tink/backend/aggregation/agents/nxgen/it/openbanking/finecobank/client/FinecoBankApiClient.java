package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.client;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.card.rpc.CardAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.card.rpc.CardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.enums.FinecoBankPaymentProduct;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.enums.FinecoBankPaymentService;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.GetPaymentAuthStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.GetPaymentAuthsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.GetPaymentStatusResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class FinecoBankApiClient {

    private final FinecoUrlProvider urlProvider;
    private final TinkHttpClient client;
    private final FinecoHeaderValues headerValues;
    private final RandomValueGenerator randomValueGenerator;

    public FinecoBankApiClient(
            FinecoUrlProvider urlProvider,
            TinkHttpClient client,
            FinecoHeaderValues headerValues,
            RandomValueGenerator randomValueGenerator) {
        this.urlProvider = urlProvider;
        this.client = client;
        this.headerValues = headerValues;
        this.randomValueGenerator = randomValueGenerator;

        this.client.addFilter(new BankServiceInternalErrorFilter());
        this.client.addFilter(new ServiceUnavailableBankServiceErrorFilter());
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(Psd2Headers.Keys.X_REQUEST_ID, randomValueGenerator.getUUID())
                .header(Psd2Headers.Keys.PSU_IP_ADDRESS, headerValues.getUserIp());
    }

    public ConsentResponse createConsent(ConsentRequest consentRequest, String state) {
        return createRequest(urlProvider.getConsentsUrl())
                .header(Psd2Headers.Keys.TPP_REDIRECT_URI, redirectUrlWithState(state))
                .body(consentRequest)
                .post(ConsentResponse.class);
    }

    public ConsentDetailsResponse getConsentDetails(String consentId) {
        return createRequest(urlProvider.getConsentDetailsUrl(consentId))
                .get(ConsentDetailsResponse.class);
    }

    public CardAccountsResponse fetchCreditCardAccounts(String consentId) {
        return createRequest(urlProvider.getCardAccountsUrl())
                .header(Psd2Headers.Keys.CONSENT_ID, consentId)
                .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                .get(CardAccountsResponse.class);
    }

    public AccountsResponse fetchAccounts(String consentId) {
        return createRequest(urlProvider.getAccountsUrl())
                .header(Psd2Headers.Keys.CONSENT_ID, consentId)
                .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                .get(AccountsResponse.class);
    }

    public PaginatorResponse getTransactions(
            String consentId, TransactionalAccount account, Date fromDate, Date toDate) {
        SimpleDateFormat paginationDateFormatter =
                new SimpleDateFormat(Formats.DEFAULT_DATE_FORMAT);

        return createRequest(urlProvider.getTransactionsUrl(account.getApiIdentifier()))
                .header(Psd2Headers.Keys.CONSENT_ID, consentId)
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

        return createRequest(urlProvider.getCardTransactionsUrl(account.getApiIdentifier()))
                .header(Psd2Headers.Keys.CONSENT_ID, consentId)
                .queryParam(QueryKeys.WITH_BALANCE, String.valueOf(true))
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOOKED)
                .queryParam(QueryKeys.DATE_FROM, paginationDateFormatter.format(fromDate))
                .get(CardTransactionsResponse.class);
    }

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest requestBody,
            FinecoBankPaymentService paymentService,
            FinecoBankPaymentProduct paymentProduct,
            String state) {
        return createRequest(
                        urlProvider.getPaymentsUrl(
                                paymentService.getValue(), paymentProduct.getValue()))
                .header(Psd2Headers.Keys.TPP_REDIRECT_URI, redirectUrlWithState(state))
                .post(CreatePaymentResponse.class, requestBody);
    }

    public GetPaymentResponse getPayment(
            FinecoBankPaymentService paymentService,
            FinecoBankPaymentProduct paymentProduct,
            String paymentId) {
        return createRequest(
                        urlProvider.getPaymentDetailsUrl(
                                paymentService.getValue(), paymentProduct.getValue(), paymentId))
                .get(GetPaymentResponse.class);
    }

    public GetPaymentStatusResponse getPaymentStatus(
            FinecoBankPaymentService paymentService,
            FinecoBankPaymentProduct paymentProduct,
            String paymentId) {
        return createRequest(
                        urlProvider.getPaymentStatusUrl(
                                paymentService.getValue(), paymentProduct.getValue(), paymentId))
                .get(GetPaymentStatusResponse.class);
    }

    public GetPaymentAuthsResponse getPaymentAuths(
            FinecoBankPaymentService paymentService,
            FinecoBankPaymentProduct paymentProduct,
            String paymentId) {
        return createRequest(
                        urlProvider.getPaymentAuthsUrl(
                                paymentService.getValue(), paymentProduct.getValue(), paymentId))
                .get(GetPaymentAuthsResponse.class);
    }

    public GetPaymentAuthStatusResponse getPaymentAuthStatus(
            FinecoBankPaymentService paymentService,
            FinecoBankPaymentProduct paymentProduct,
            String paymentId,
            String authId) {
        return createRequest(
                        urlProvider.getPaymentAuthStatusUrl(
                                paymentService.getValue(),
                                paymentProduct.getValue(),
                                paymentId,
                                authId))
                .get(GetPaymentAuthStatusResponse.class);
    }

    private URL redirectUrlWithState(String state) {
        return new URL(headerValues.getRedirectUrl()).queryParam(QueryKeys.STATE, state);
    }
}
