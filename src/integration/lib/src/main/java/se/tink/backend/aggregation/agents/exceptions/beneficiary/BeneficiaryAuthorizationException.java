package se.tink.backend.aggregation.agents.exceptions.beneficiary;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.error.OpenIdError;

public class BeneficiaryAuthorizationException extends BeneficiaryException {
    public static final String DEFAULT_MESSAGE =
            "Beneficiary request was not authorised. Please try again.";
    protected OpenIdError openIdError;

    public BeneficiaryAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeneficiaryAuthorizationException(String message) {
        super(message);
    }

    public BeneficiaryAuthorizationException() {
        super(DEFAULT_MESSAGE);
    }

    public BeneficiaryAuthorizationException(OpenIdError openIdError, String message) {
        super(message);
        this.openIdError = openIdError;
    }
}
