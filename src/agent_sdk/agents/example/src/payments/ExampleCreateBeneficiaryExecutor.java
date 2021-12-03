package se.tink.agent.agents.example.payments;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryException;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryResponse;

public class ExampleCreateBeneficiaryExecutor implements CreateBeneficiaryExecutor {

    @Override
    public CreateBeneficiaryResponse createBeneficiary(
            CreateBeneficiaryRequest createBeneficiaryRequest) throws BeneficiaryException {
        throw new UnsupportedOperationException();
    }

    @Override
    public CreateBeneficiaryMultiStepResponse sign(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest)
            throws BeneficiaryException, AuthenticationException {
        throw new UnsupportedOperationException();
    }
}
