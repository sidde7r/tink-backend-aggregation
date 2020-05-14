package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;

public class AddBeneficiaryController {
    private final AddBeneficiaryExecutor addBeneficiaryExecutor;

    public AddBeneficiaryController(AddBeneficiaryExecutor addBeneficiaryExecutor) {
        this.addBeneficiaryExecutor = addBeneficiaryExecutor;
    }

    public AddBeneficiaryResponse createBeneficiary(AddBeneficiaryRequest addBeneficiaryRequest)
            throws PaymentException {
        return addBeneficiaryExecutor.createBeneficiary(addBeneficiaryRequest);
    }

    public CreateBeneficiaryMultiStepResponse sign(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest)
            throws PaymentException {
        try {
            return addBeneficiaryExecutor.sign(createBeneficiaryMultiStepRequest);
        } catch (AuthenticationException e) {
            if (e instanceof BankIdException) {
                BankIdError bankIdError = ((BankIdException) e).getError();
                switch (bankIdError) {
                    case CANCELLED:
                        throw new PaymentAuthorizationException(
                                PaymentConstants.BankId.CANCELLED, e);
                    case NO_CLIENT:
                        throw new PaymentAuthorizationException(
                                PaymentConstants.BankId.NO_CLIENT, e);
                    case TIMEOUT:
                        throw new PaymentAuthorizationException(PaymentConstants.BankId.TIMEOUT, e);
                    case INTERRUPTED:
                        throw new PaymentAuthorizationException(
                                PaymentConstants.BankId.INTERRUPTED, e);
                    case UNKNOWN:
                    default:
                        throw new PaymentAuthorizationException(PaymentConstants.BankId.UNKNOWN, e);
                }
            }

            throw new PaymentAuthorizationException("Beneficiary request could not be signed", e);
        }
    }
}
