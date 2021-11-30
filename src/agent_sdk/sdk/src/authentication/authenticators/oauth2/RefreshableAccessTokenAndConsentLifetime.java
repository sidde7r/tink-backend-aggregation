package se.tink.agent.sdk.authentication.authenticators.oauth2;

import lombok.Builder;
import lombok.Getter;
import se.tink.agent.sdk.authentication.new_consent.ConsentLifetime;
import se.tink.agent.sdk.models.authentication.RefreshableAccessToken;

@Builder
@Getter
public class RefreshableAccessTokenAndConsentLifetime {
    private final RefreshableAccessToken token;
    private final ConsentLifetime consentLifetime;
}
