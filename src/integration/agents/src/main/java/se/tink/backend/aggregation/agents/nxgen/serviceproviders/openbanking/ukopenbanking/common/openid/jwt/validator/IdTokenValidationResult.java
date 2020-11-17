package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.validator;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdTokenValidationResult {

    private final ValidationResult signatureValid;

    private ValidationResult atHashValid;

    private ValidationResult cHashValid;

    private ValidationResult sHashValid;

    private String errorMessage;

    @Override
    public String toString() {
        return "IdTokenValidationResult{"
                + "signatureValid="
                + signatureValid
                + ", atHashValid="
                + atHashValid
                + ", cHashValid="
                + cHashValid
                + ", sHashValid="
                + sHashValid
                + (errorMessage != null ? ", errorMessage=" + errorMessage : "")
                + '}';
    }

    public boolean isValid() {
        return signatureValid.equals(ValidationResult.VALID)
                        && atHashValid.equals(ValidationResult.SKIPPED)
                || atHashValid.equals(ValidationResult.VALID)
                        && cHashValid.equals(ValidationResult.SKIPPED)
                || cHashValid.equals(ValidationResult.VALID)
                        && sHashValid.equals(ValidationResult.SKIPPED)
                || sHashValid.equals(ValidationResult.VALID);
    }

    public enum ValidationResult {
        VALID,
        INVALID,
        ERROR,
        SKIPPED
    }
}
