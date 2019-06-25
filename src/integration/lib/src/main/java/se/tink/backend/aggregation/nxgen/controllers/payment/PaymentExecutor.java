package se.tink.backend.aggregation.nxgen.controllers.payment;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;

public interface PaymentExecutor {

    PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException;

    PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException;

    CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest);

    PaymentResponse cancel(PaymentRequest paymentRequest);

    Optional<PaymentController> constructPaymentController();
}
