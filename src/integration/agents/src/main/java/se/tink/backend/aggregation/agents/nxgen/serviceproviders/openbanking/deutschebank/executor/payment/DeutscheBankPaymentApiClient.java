package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.executor.payment;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.Parameters;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheHeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheMarketConfiguration;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.PaymentProduct;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.PaymentService;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.FetchPaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

public class DeutscheBankPaymentApiClient extends DeutscheBankApiClient
        implements PaymentApiClient {
    private final Credentials credentials;
    private final StrongAuthenticationState authenticationState;
    private final DeutscheBankPaymentMapper paymentMapper;

    public DeutscheBankPaymentApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            DeutscheMarketConfiguration marketConfiguration,
            DeutscheHeaderValues headerValues,
            Credentials credentials,
            StrongAuthenticationState authenticationState,
            RandomValueGenerator randomValueGenerator,
            DeutscheBankPaymentMapper paymentMapper) {
        super(client, persistentStorage, headerValues, marketConfiguration, randomValueGenerator);
        this.credentials = credentials;
        this.authenticationState = authenticationState;
        this.paymentMapper = paymentMapper;
    }

    @Override
    public CreatePaymentResponse createPayment(PaymentRequest paymentRequest) {
        String psuId = credentials.getField(Key.USERNAME);
        URL redirectWithState =
                new URL(headerValues.getRedirectUrl())
                        .queryParam(QueryKeys.STATE, authenticationState.getState());
        CreatePaymentRequest createPaymentRequest = buildPaymentRequestBody(paymentRequest);

        return createRequestWithServiceMapped(
                        new URL(marketConfiguration.getBaseUrl() + Urls.PAYMENT)
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
                .header(HeaderKeys.PSU_ID_TYPE, marketConfiguration.getPsuIdType())
                .header(HeaderKeys.PSU_ID, psuId)
                .header(HeaderKeys.TPP_REDIRECT_URI, redirectWithState)
                .header(HeaderKeys.TPP_NOK_REDIRECT_URI, redirectWithState)
                .post(CreatePaymentResponse.class, createPaymentRequest);
    }

    private CreatePaymentRequest buildPaymentRequestBody(PaymentRequest paymentRequest) {
        return PaymentServiceType.PERIODIC.equals(
                        paymentRequest.getPayment().getPaymentServiceType())
                ? paymentMapper.getRecurringPaymentRequest(paymentRequest.getPayment())
                : paymentMapper.getPaymentRequest(paymentRequest.getPayment());
    }

    @Override
    public FetchPaymentStatusResponse fetchPaymentStatus(PaymentRequest paymentRequest) {
        String paymentId = paymentRequest.getPayment().getUniqueId();
        return createRequestWithServiceMapped(
                        new URL(marketConfiguration.getBaseUrl() + Urls.PAYMENT_STATUS)
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

    @Override
    protected RequestBuilder createRequestWithServiceMapped(URL url) {
        return createRequest(url.parameter(Parameters.SERVICE_KEY, Parameters.PIS));
    }
}
