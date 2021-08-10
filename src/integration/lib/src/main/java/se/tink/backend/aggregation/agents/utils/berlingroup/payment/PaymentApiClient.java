package se.tink.backend.aggregation.agents.utils.berlingroup.payment;

import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.FetchPaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

public interface PaymentApiClient {

    CreatePaymentResponse createPayment(PaymentRequest paymentRequest);

    FetchPaymentStatusResponse fetchPaymentStatus(PaymentRequest paymentRequest);
}
