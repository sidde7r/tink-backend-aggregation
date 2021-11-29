package se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps;

import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppInitAuthentication;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppResult;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppStatus;
import se.tink.agent.sdk.authentication.new_consent.NewConsentRequest;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;
import se.tink.agent.sdk.authentication.new_consent.response.NewConsentResponse;
import se.tink.agent.sdk.storage.SerializableReference;

public class ThirdPartyAppInitStep implements NewConsentStep {
    private final ThirdPartyAppInitAuthentication agentInitAuthentication;
    private final Class<? extends NewConsentStep> nextStep;

    public ThirdPartyAppInitStep(
            ThirdPartyAppInitAuthentication agentInitAuthentication,
            Class<? extends NewConsentStep> nextStep) {
        this.agentInitAuthentication = agentInitAuthentication;
        this.nextStep = nextStep;
    }

    @Override
    public NewConsentResponse execute(NewConsentRequest request) {
        ThirdPartyAppResult thirdPartyAppResult =
                this.agentInitAuthentication.initThirdPartyAppAuthentication();
        handleStatus(thirdPartyAppResult.getStatus());

        SerializableReference reference = thirdPartyAppResult.getReference();
        request.getAuthenticationStorage()
                .put(ThirdPartyAppAuthenticator.STATE_KEY_REFERENCE, reference);

        return NewConsentResponse.nextStep(this.nextStep).noUserInteraction().build();
    }

    private void handleStatus(ThirdPartyAppStatus status) {
        // TODO: use proper exceptions.
        switch (status) {
            case PENDING:
                break;

            case NO_CLIENT:
                throw new IllegalStateException("NO_CLIENT");

            case ALREADY_IN_PROGRESS:
                throw new IllegalStateException("ALREADY_IN_PROGRESS");

            case UNKNOWN_FAILURE:
                throw new IllegalStateException("UNKNOWN_FAILURE");

            default:
                throw new IllegalStateException(
                        String.format("Unexpected and invalid status: %s.", status));
        }
    }
}
