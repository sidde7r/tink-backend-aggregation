package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag;

import static se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants.PathVariables.PAYMENT_PRODUCT;
import static se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants.PathVariables.PAYMENT_SERVICE;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.Urls;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants.PathVariables;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.PaymentProduct;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.PaymentService;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.FetchPaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

@RequiredArgsConstructor
public class BankverlagPaymentApiClient implements PaymentApiClient {

    private final PaymentMapper<CreatePaymentRequest> paymentMapper;
    private final BankverlagRequestBuilder requestBuilder;

    @Override
    public CreatePaymentResponse createPayment(PaymentRequest paymentRequest) {
        CreatePaymentRequest createPaymentRequest = buildPaymentRequestBody(paymentRequest);

        return requestBuilder
                .createRequest(
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
                                .getUrl())
                .body(createPaymentRequest)
                .post(CreatePaymentResponse.class);
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
