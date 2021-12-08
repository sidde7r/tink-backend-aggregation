package se.tink.agent.sdk.authentication.authenticators.swedish_mobile_bankid.steps;

import se.tink.agent.sdk.authentication.authenticators.swedish_mobile_bankid.SwedishMobileBankIdGetAutostartToken;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppAuthenticator;
import se.tink.agent.sdk.authentication.new_consent.NewConsentRequest;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;
import se.tink.agent.sdk.authentication.new_consent.response.NewConsentResponse;
import se.tink.agent.sdk.storage.SerializableReference;
import se.tink.agent.sdk.user_interaction.UserInteraction;

public class SwedishMobileBankIdOpenAppStep implements NewConsentStep {

    private final SwedishMobileBankIdGetAutostartToken agentGetAutostartToken;
    private final Class<? extends NewConsentStep> nextStep;

    public SwedishMobileBankIdOpenAppStep(
            SwedishMobileBankIdGetAutostartToken agentGetAutostartToken,
            Class<? extends NewConsentStep> nextStep) {
        this.agentGetAutostartToken = agentGetAutostartToken;
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

        UserInteraction<String> swedishMobileBankIdAutostartToken =
                this.agentGetAutostartToken.getAutostartToken(reference);

        return NewConsentResponse.nextStep(this.nextStep)
                .userInteraction(swedishMobileBankIdAutostartToken)
                .build();
    }
}
