package se.tink.agent.sdk.authentication.common_steps;

import se.tink.agent.sdk.authentication.new_consent.ConsentLifetime;
import se.tink.agent.sdk.authentication.new_consent.NewConsentRequest;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;
import se.tink.agent.sdk.authentication.new_consent.response.NewConsentResponse;

public class GetConsentLifetimeStep implements NewConsentStep {
    private final GetConsentLifetime agentGetConsentLifetime;

    public GetConsentLifetimeStep(GetConsentLifetime agentGetConsentLifetime) {
        this.agentGetConsentLifetime = agentGetConsentLifetime;
    }

    @Override
    public NewConsentResponse execute(NewConsentRequest request) {
        ConsentLifetime consentLifetime = this.agentGetConsentLifetime.getConsentLifetime();
        return NewConsentResponse.done(consentLifetime);
    }
}
