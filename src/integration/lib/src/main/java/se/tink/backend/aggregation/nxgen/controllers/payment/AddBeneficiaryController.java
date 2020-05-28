package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;

public class AddBeneficiaryController {
    private final AddBeneficiaryExecutor addBeneficiaryExecutor;

    public AddBeneficiaryController(AddBeneficiaryExecutor addBeneficiaryExecutor) {
        this.addBeneficiaryExecutor = addBeneficiaryExecutor;
    }

    public AddBeneficiaryResponse createBeneficiary(AddBeneficiaryRequest addBeneficiaryRequest)
            throws BeneficiaryException {
        return addBeneficiaryExecutor.createBeneficiary(addBeneficiaryRequest);
    }

    public CreateBeneficiaryMultiStepResponse sign(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest)
            throws BeneficiaryException {
        try {
            return addBeneficiaryExecutor.sign(createBeneficiaryMultiStepRequest);
        } catch (AuthenticationException e) {
            if (e instanceof BankIdException) {
                BankIdError bankIdError = ((BankIdException) e).getError();
                switch (bankIdError) {
                    case CANCELLED:
                        throw new BeneficiaryAuthorizationException(
                                PaymentConstants.BankId.CANCELLED, e);
                    case NO_CLIENT:
                        throw new BeneficiaryAuthorizationException(
                                PaymentConstants.BankId.NO_CLIENT, e);
                    case TIMEOUT:
                        throw new BeneficiaryAuthorizationException(
                                PaymentConstants.BankId.TIMEOUT, e);
                    case INTERRUPTED:
                        throw new BeneficiaryAuthorizationException(
                                PaymentConstants.BankId.INTERRUPTED, e);
                    case UNKNOWN:
                    default:
                        throw new BeneficiaryAuthorizationException(
                                PaymentConstants.BankId.UNKNOWN, e);
                }
            }

            throw new BeneficiaryAuthorizationException(
                    "Beneficiary request could not be signed", e);
        }
    }
}
