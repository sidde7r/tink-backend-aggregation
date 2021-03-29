package se.tink.backend.aggregation.agents.utils.berlingroup.payment;

import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.FetchPaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

public interface PaymentApiClient {

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest request, PaymentRequest paymentRequest);

    public FetchPaymentStatusResponse fetchPaymentStatus(PaymentRequest paymentRequest);
}
