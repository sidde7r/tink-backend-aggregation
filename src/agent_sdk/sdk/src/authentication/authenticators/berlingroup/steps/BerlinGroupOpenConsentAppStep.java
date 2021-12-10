package se.tink.agent.sdk.authentication.authenticators.berlingroup.steps;

import java.time.LocalDate;
import java.time.ZoneId;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupAuthenticatorConfiguration;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupConsent;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupCreateConsent;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupGetConfiguration;
import se.tink.agent.sdk.authentication.new_consent.NewConsentRequest;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;
import se.tink.agent.sdk.authentication.new_consent.response.NewConsentResponse;
import se.tink.agent.sdk.operation.MultifactorAuthenticationState;
import se.tink.agent.sdk.user_interaction.ThirdPartyAppInfo;
import se.tink.agent.sdk.user_interaction.UserInteraction;
import se.tink.agent.sdk.utils.TimeGenerator;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BerlinGroupOpenConsentAppStep implements NewConsentStep {

    private final TimeGenerator timeGenerator;
    private final MultifactorAuthenticationState multifactorAuthenticationState;
    private final BerlinGroupGetConfiguration agentGetConfiguration;
    private final BerlinGroupCreateConsent agentCreateConsent;
    private final Class<? extends NewConsentStep> handleCallbackDataStep;

    public BerlinGroupOpenConsentAppStep(
            TimeGenerator timeGenerator,
            MultifactorAuthenticationState multifactorAuthenticationState,
            BerlinGroupGetConfiguration agentGetConfiguration,
            BerlinGroupCreateConsent agentCreateConsent,
            Class<? extends NewConsentStep> handleCallbackDataStep) {
        this.timeGenerator = timeGenerator;
        this.multifactorAuthenticationState = multifactorAuthenticationState;
        this.agentGetConfiguration = agentGetConfiguration;
        this.agentCreateConsent = agentCreateConsent;
        this.handleCallbackDataStep = handleCallbackDataStep;
    }

    @Override
    public NewConsentResponse execute(NewConsentRequest request) {
        // Calculate the new consent's valid until date.
        BerlinGroupAuthenticatorConfiguration configuration =
                this.agentGetConfiguration.getConfiguration();
        LocalDate consentValidUntil =
                timeGenerator
                        .localDateNow(ZoneId.of("UTC"))
                        .plus(configuration.getConsentValidForPeriod());

        BerlinGroupConsent response =
                this.agentCreateConsent.createConsent(
                        this.multifactorAuthenticationState.getState(), consentValidUntil);

        request.getAuthenticationStorage()
                .put(BerlinGroupAuthenticator.STATE_KEY_CONSENT_ID, response.getConsentId());

        UserInteraction<ThirdPartyAppInfo> thirdPartyAppUserInteraction =
                buildThirdPartyApp(response.getConsentAppUrl());

        return NewConsentResponse.nextStep(this.handleCallbackDataStep)
                .userInteraction(thirdPartyAppUserInteraction)
                .build();
    }

    private UserInteraction<ThirdPartyAppInfo> buildThirdPartyApp(URL consentAppUrl) {
        ThirdPartyAppInfo thirdPartyAppInfo = ThirdPartyAppInfo.of(consentAppUrl);
        return this.multifactorAuthenticationState.intoUserInteraction(thirdPartyAppInfo);
    }
}
