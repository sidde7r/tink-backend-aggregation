package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.enums.FinecoBankPaymentProduct;
import se.tink.backend.aggregation.nxgen.controllers.payment.FetchablePaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

@RequiredArgsConstructor
public class FinecoPaymentFetcher implements FetchablePaymentExecutor {

    private final FinecoBankApiClient apiClient;

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return apiClient
                .getPayment(
                        FinecoBankPaymentProduct.fromTinkPayment(paymentRequest.getPayment()),
                        paymentRequest.getPayment().getUniqueId())
                .toTinkPaymentResponse(paymentRequest);
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        return new PaymentListResponse(
                paymentListRequest.getPaymentRequestList().stream()
                        .map(this::fetch)
                        .collect(Collectors.toList()));
    }
}
