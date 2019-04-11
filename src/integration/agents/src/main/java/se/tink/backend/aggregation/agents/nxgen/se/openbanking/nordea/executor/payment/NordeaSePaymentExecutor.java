package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment;

import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

public class NordeaSePaymentExecutor implements PaymentExecutor {

    @Override
    public PaymentResponse createPayment(PaymentRequest payment) {
        return null;
    }

    @Override
    public PaymentResponse fetchPaymentStatus(PaymentRequest payment) {
        return null;
    }

    @Override
    public PaymentMultiStepResponse signPayment(PaymentRequest payment) {
        return null;
    }

    @Override
    public PaymentMultiStepResponse createBeneficiary() {
        return null;
    }

    @Override
    public PaymentResponse cancelPayment(PaymentRequest payment) {
        return null;
    }

    @Override
    public PaymentListResponse fetchPayments() {
        return null;
    }
}
