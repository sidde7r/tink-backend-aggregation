package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class N26ValidateConsentParameters {
    private final String consentId;
    private final String accessToken;
}
