package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.client;

import static se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants.PathVariables.PAYMENT_PRODUCT;
import static se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants.PathVariables.PAYMENT_SERVICE;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.authenticator.SpardaRedirectUrlBuilder;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants.PathVariables;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.PaymentProduct;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.PaymentService;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.FetchPaymentStatusResponse;
import se.tink.backend.aggregation.api.Psd2Headers.Keys;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

@RequiredArgsConstructor
public class SpardaPaymentApiClient implements PaymentApiClient {

    private final SpardaRequestBuilder requestBuilder;
    private final SpardaErrorHandler errorHandler;
    private final String redirectUrl;
    private final String bicCode;
    private final PaymentMapper<CreatePaymentRequest> paymentMapper;
    private final SpardaRedirectUrlBuilder redirectUrlBuilder;

    @Override
    public CreatePaymentResponse createPayment(PaymentRequest paymentRequest) {
        CreatePaymentRequest createPaymentRequest = buildPaymentRequestBody(paymentRequest);

        CreatePaymentResponse paymentResponse =
                requestBuilder
                        .createRequest(
                                Urls.CREATE_PAYMENT
                                        .parameter(
                                                PAYMENT_SERVICE,
                                                PaymentService.getPaymentService(
                                                        paymentRequest
                                                                .getPayment()
                                                                .getPaymentServiceType()))
                                        .parameter(
                                                PAYMENT_PRODUCT,
                                                PaymentProduct.getPaymentProduct(
                                                        paymentRequest
                                                                .getPayment()
                                                                .getPaymentScheme())))
                        .header(Keys.TPP_REDIRECT_URI, redirectUrl)
                        .header("X-BIC", bicCode)
                        .body(createPaymentRequest)
                        .post(CreatePaymentResponse.class);

        String url =
                redirectUrlBuilder
                        .buildPaymentAuthorizeUrl(paymentResponse.getLinks().getScaRedirect())
                        .toString();
        paymentResponse.getLinks().setScaRedirect(url);

        return paymentResponse;
    }

    private CreatePaymentRequest buildPaymentRequestBody(PaymentRequest paymentRequest) {
        return PaymentServiceType.PERIODIC.equals(
                        paymentRequest.getPayment().getPaymentServiceType())
                ? paymentMapper.getRecurringPaymentRequest(paymentRequest.getPayment())
                : paymentMapper.getPaymentRequest(paymentRequest.getPayment());
    }

    @Override
    public FetchPaymentStatusResponse fetchPaymentStatus(PaymentRequest paymentRequest) {
        return requestBuilder
                .createRequest(
                        Urls.GET_PAYMENT_STATUS
                                .parameter(
                                        PAYMENT_SERVICE,
                                        PaymentService.getPaymentService(
                                                paymentRequest
                                                        .getPayment()
                                                        .getPaymentServiceType()))
                                .parameter(
                                        PAYMENT_PRODUCT,
                                        PaymentProduct.getPaymentProduct(
                                                paymentRequest.getPayment().getPaymentScheme()))
                                .parameter(
                                        PathVariables.PAYMENT_ID,
                                        paymentRequest.getPayment().getUniqueId()))
                .get(FetchPaymentStatusResponse.class);
    }
}
