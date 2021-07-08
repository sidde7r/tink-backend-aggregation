package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.PaymentProduct;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.PaymentService;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.FetchPaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

@Slf4j
@RequiredArgsConstructor
public final class DkbApiClient implements PaymentApiClient {

    private final TinkHttpClient client;
    private final DkbStorage storage;
    private final DkbUserIpInformation dkbUserIpInformation;
    private final RandomValueGenerator randomValueGenerator;
    private final PaymentMapper<CreatePaymentRequest> paymentRequestMapper;

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        RequestBuilder requestBuilder =
                createRequest(url)
                        .header(
                                HeaderKeys.PSD_2_AUTHORIZATION_HEADER,
                                storage.getAccessToken()
                                        .map(OAuth2Token::toAuthorizeHeader)
                                        .orElse(null))
                        .header(HeaderKeys.X_REQUEST_ID, randomValueGenerator.getUUID().toString());
        addPsuIpAddressHeaderIfManualRefresh(requestBuilder);
        return requestBuilder;
    }

    private void addPsuIpAddressHeaderIfManualRefresh(RequestBuilder requestBuilder) {
        if (dkbUserIpInformation.isUserPresent()) {
            log.info("Request is attended -- adding PSU header");
            requestBuilder.header(HeaderKeys.PSU_IP_ADDRESS, dkbUserIpInformation.getUserIp());
        } else {
            log.info("Request is unattended -- omitting PSU header");
        }
    }

    private RequestBuilder createFetchingRequest(URL url) {
        return createRequestInSession(url).header(HeaderKeys.CONSENT_ID, getConsent());
    }

    private String getConsent() {
        return storage.getConsentId()
                .orElseThrow(() -> new NoSuchElementException("Can't obtain consent id"));
    }

    public GetAccountsResponse getAccounts() {
        return createFetchingRequest(Urls.GET_ACCOUNTS).get(GetAccountsResponse.class);
    }

    public GetBalancesResponse getBalances(String accountId) {
        return createFetchingRequest(Urls.GET_BALANCES.parameter(IdTags.ACCOUNT_ID, accountId))
                .get(GetBalancesResponse.class);
    }

    public GetTransactionsResponse getTransactions(
            TransactionalAccount account, LocalDate fromDate, LocalDate toDate) {

        return createFetchingRequest(
                        Urls.GET_TRANSACTIONS.parameter(
                                IdTags.ACCOUNT_ID, account.getApiIdentifier()))
                .queryParam(QueryKeys.DATE_FROM, fromDate.toString())
                .queryParam(QueryKeys.DATE_TO, toDate.toString())
                .queryParam(QueryKeys.BOOKING_STATUS, QueryValues.BOTH)
                .get(GetTransactionsResponse.class);
    }

    @Override
    public CreatePaymentResponse createPayment(PaymentRequest paymentRequest) {
        CreatePaymentRequest createPaymentRequest =
                PaymentServiceType.PERIODIC.equals(
                                paymentRequest.getPayment().getPaymentServiceType())
                        ? paymentRequestMapper.getRecurringPaymentRequest(
                                paymentRequest.getPayment())
                        : paymentRequestMapper.getPaymentRequest(paymentRequest.getPayment());
        return createRequestInSession(
                        DkbConstants.Urls.PAYMENT_INITIATION
                                .parameter(
                                        PaymentConstants.PathVariables.PAYMENT_SERVICE,
                                        PaymentService.getPaymentService(
                                                paymentRequest
                                                        .getPayment()
                                                        .getPaymentServiceType()))
                                .parameter(
                                        PaymentConstants.PathVariables.PAYMENT_PRODUCT,
                                        PaymentProduct.getPaymentProduct(
                                                paymentRequest.getPayment().getPaymentScheme())))
                .header(PaymentConstants.HeaderKeys.TPP_REJECTION_NOFUNDS_PREFERRED, true)
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    @Override
    public FetchPaymentStatusResponse fetchPaymentStatus(PaymentRequest paymentRequest) {
        String paymentId = paymentRequest.getPayment().getUniqueId();
        return createRequestInSession(
                        DkbConstants.Urls.FETCH_PAYMENT_STATUS
                                .parameter(
                                        PaymentConstants.PathVariables.PAYMENT_SERVICE,
                                        PaymentService.getPaymentService(
                                                paymentRequest
                                                        .getPayment()
                                                        .getPaymentServiceType()))
                                .parameter(
                                        PaymentConstants.PathVariables.PAYMENT_PRODUCT,
                                        PaymentProduct.getPaymentProduct(
                                                paymentRequest.getPayment().getPaymentScheme()))
                                .parameter(PaymentConstants.PathVariables.PAYMENT_ID, paymentId))
                .get(FetchPaymentStatusResponse.class);
    }
}
