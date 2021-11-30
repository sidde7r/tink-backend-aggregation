package se.tink.agent.sdk.authentication.authenticators.oauth2;

import lombok.Builder;
import lombok.Getter;
import se.tink.agent.sdk.authentication.new_consent.ConsentLifetime;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@Builder
@Getter
public class AccessTokenAndConsentLifetime {
    private final OAuth2Token token;
    private final ConsentLifetime consentLifetime;
}
