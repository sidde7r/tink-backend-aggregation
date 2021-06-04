package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;

public interface PaymentExecutor {

    PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException;

    PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException, AuthenticationException;

    CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest);

    PaymentResponse cancel(PaymentRequest paymentRequest) throws PaymentException;
}
