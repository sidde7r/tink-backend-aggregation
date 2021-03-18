package se.tink.backend.aggregation.agents.exceptions.beneficiary;

import se.tink.libraries.account.enums.AccountIdentifierType;

public class BeneficiaryInvalidAccountTypeException extends BeneficiaryException {
    private static final String DEFAULT_MESSAGE =
            "Beneficiary request was of wrong account type %s.";

    public BeneficiaryInvalidAccountTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeneficiaryInvalidAccountTypeException(String message) {
        super(message);
    }

    public BeneficiaryInvalidAccountTypeException(AccountIdentifierType type) {
        super(String.format(DEFAULT_MESSAGE, type.toString()));
    }
}
