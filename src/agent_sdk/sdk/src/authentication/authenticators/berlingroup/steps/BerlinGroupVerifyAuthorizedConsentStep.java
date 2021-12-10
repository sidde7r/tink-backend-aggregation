package se.tink.agent.sdk.authentication.authenticators.berlingroup.steps;

import java.time.Duration;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupAuthenticatorConfiguration;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupGetConfiguration;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupGetConsentStatus;
import se.tink.agent.sdk.authentication.existing_consent.ConsentStatus;
import se.tink.agent.sdk.authentication.new_consent.ConsentLifetime;
import se.tink.agent.sdk.authentication.new_consent.NewConsentRequest;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;
import se.tink.agent.sdk.authentication.new_consent.response.NewConsentResponse;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;

public class BerlinGroupVerifyAuthorizedConsentStep implements NewConsentStep {

    private final BerlinGroupGetConfiguration agentGetConfiguration;
    private final BerlinGroupGetConsentStatus agentGetConsentStatus;

    public BerlinGroupVerifyAuthorizedConsentStep(
            BerlinGroupGetConfiguration agentGetConfiguration,
            BerlinGroupGetConsentStatus agentGetConsentStatus) {
        this.agentGetConfiguration = agentGetConfiguration;
        this.agentGetConsentStatus = agentGetConsentStatus;
    }

    @Override
    public NewConsentResponse execute(NewConsentRequest request) {
        if (!request.getUserResponseData().isPresent()) {
            throw ThirdPartyAppError.TIMED_OUT.exception();
        }

        String consentId =
                request.getAuthenticationStorage()
                        .tryGet(BerlinGroupAuthenticator.STATE_KEY_CONSENT_ID)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "ConsentId was not present in AuthenticationStorage."));

        ConsentStatus consentStatus = agentGetConsentStatus.getConsentStatus(consentId);
        if (!ConsentStatus.VALID.equals(consentStatus)) {
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
        }

        // Write the `consentId` into the agent storage on a key picked by the agent.
        BerlinGroupAuthenticatorConfiguration configuration =
                this.agentGetConfiguration.getConfiguration();
        request.getAgentStorage().put(configuration.getConsentIdStorageKey(), consentId);

        Duration consentLifetime =
                Duration.ofDays(configuration.getConsentValidForPeriod().getDays());
        return NewConsentResponse.done(ConsentLifetime.specificLifetime(consentLifetime));
    }
}
