package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryException;

public interface AddBeneficiaryExecutor {
    AddBeneficiaryResponse createBeneficiary(AddBeneficiaryRequest addBeneficiaryRequest)
            throws BeneficiaryException;

    CreateBeneficiaryMultiStepResponse sign(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest)
            throws BeneficiaryException, AuthenticationException;
}
