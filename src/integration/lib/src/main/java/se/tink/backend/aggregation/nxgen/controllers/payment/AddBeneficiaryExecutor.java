package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;

public interface AddBeneficiaryExecutor {
    AddBeneficiaryResponse createBeneficiary(AddBeneficiaryRequest addBeneficiaryRequest)
            throws PaymentException;

    CreateBeneficiaryMultiStepResponse sign(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest)
            throws PaymentException, AuthenticationException;
}
