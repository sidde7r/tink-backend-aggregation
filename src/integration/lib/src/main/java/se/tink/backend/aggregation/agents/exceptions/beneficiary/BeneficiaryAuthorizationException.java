package se.tink.backend.aggregation.agents.exceptions.beneficiary;

import se.tink.backend.aggregation.agents.exceptions.entity.ErrorEntity;

public class BeneficiaryAuthorizationException extends BeneficiaryException {
    public static final String DEFAULT_MESSAGE =
            "Beneficiary request was not authorised. Please try again.";
    protected ErrorEntity errorEntity;

    public BeneficiaryAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeneficiaryAuthorizationException(String message) {
        super(message);
    }
}
