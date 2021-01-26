package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;

@Getter
public class ValidateConsentCombinedResponse {
    private ConsentDetailsResponse validResponse;
    private ValidateConsentErrorResponse errorResponse;

    public ValidateConsentCombinedResponse(ConsentDetailsResponse validResponse) {
        this.validResponse = validResponse;
    }

    public ValidateConsentCombinedResponse(ValidateConsentErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }

    public boolean hasValidDetails() {
        return validResponse != null && validResponse.isValid();
    }

    public boolean isLoginExpired() {
        return errorResponse != null && errorResponse.isLoginExpired();
    }
}
