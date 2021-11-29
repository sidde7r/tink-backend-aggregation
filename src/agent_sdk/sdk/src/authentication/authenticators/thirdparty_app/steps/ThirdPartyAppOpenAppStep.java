package se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps;

import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppGetAppInfo;
import se.tink.agent.sdk.authentication.new_consent.NewConsentRequest;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;
import se.tink.agent.sdk.authentication.new_consent.response.NewConsentResponse;
import se.tink.agent.sdk.storage.SerializableReference;
import se.tink.agent.sdk.user_interaction.ThirdPartyAppInfo;
import se.tink.agent.sdk.user_interaction.UserInteraction;

public class ThirdPartyAppOpenAppStep implements NewConsentStep {

    private final ThirdPartyAppGetAppInfo agentGetAppInfo;
    private final Class<? extends NewConsentStep> nextStep;

    public ThirdPartyAppOpenAppStep(
            ThirdPartyAppGetAppInfo agentGetAppInfo, Class<? extends NewConsentStep> nextStep) {
        this.agentGetAppInfo = agentGetAppInfo;
        this.nextStep = nextStep;
    }

    @Override
    public NewConsentResponse execute(NewConsentRequest request) {
        SerializableReference reference =
                request.getAuthenticationStorage()
                        .tryGet(
                                ThirdPartyAppAuthenticator.STATE_KEY_REFERENCE,
                                SerializableReference.class)
                        .orElse(null);

        UserInteraction<ThirdPartyAppInfo> thirdPartyAppInfo =
                this.agentGetAppInfo.getThirdPartyAppInfo(reference);

        return NewConsentResponse.nextStep(this.nextStep)
                .userInteraction(thirdPartyAppInfo)
                .build();
    }
}
