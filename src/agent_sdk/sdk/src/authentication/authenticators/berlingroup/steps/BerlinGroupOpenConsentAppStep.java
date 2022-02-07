package se.tink.agent.sdk.authentication.authenticators.berlingroup.steps;

import java.time.LocalDate;
import java.time.ZoneId;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupAuthenticatorConfiguration;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupConsent;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupCreateConsent;
import se.tink.agent.sdk.authentication.authenticators.berlingroup.BerlinGroupGetConfiguration;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.interactive_step.IntermediateStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.IntermediateStepResponse;
import se.tink.agent.sdk.user_interaction.ThirdPartyAppInfo;
import se.tink.agent.sdk.user_interaction.UserInteraction;
import se.tink.agent.sdk.utils.RandomGenerator;
import se.tink.agent.sdk.utils.TimeGenerator;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BerlinGroupOpenConsentAppStep extends IntermediateStep {

    private final TimeGenerator timeGenerator;
    private final RandomGenerator randomGenerator;
    private final BerlinGroupGetConfiguration agentGetConfiguration;
    private final BerlinGroupCreateConsent agentCreateConsent;
    private final Class<? extends BaseStep<?, ?>> handleCallbackDataStep;

    public BerlinGroupOpenConsentAppStep(
            TimeGenerator timeGenerator,
            RandomGenerator randomGenerator,
            BerlinGroupGetConfiguration agentGetConfiguration,
            BerlinGroupCreateConsent agentCreateConsent,
            Class<? extends BaseStep<?, ?>> handleCallbackDataStep) {
        this.timeGenerator = timeGenerator;
        this.randomGenerator = randomGenerator;
        this.agentGetConfiguration = agentGetConfiguration;
        this.agentCreateConsent = agentCreateConsent;
        this.handleCallbackDataStep = handleCallbackDataStep;
    }

    @Override
    public IntermediateStepResponse execute(StepRequest<Void> request) {
        // Calculate the new consent's valid until date.
        BerlinGroupAuthenticatorConfiguration configuration =
                this.agentGetConfiguration.getConfiguration();
        LocalDate consentValidUntil =
                timeGenerator
                        .localDateNow(ZoneId.of("UTC"))
                        .plus(configuration.getConsentValidForPeriod());

        String state = this.randomGenerator.randomUuidWithTinkTag();
        BerlinGroupConsent response =
                this.agentCreateConsent.createConsent(state, consentValidUntil);

        request.getStepStorage()
                .put(BerlinGroupAuthenticator.STATE_KEY_CONSENT_ID, response.getConsentId());

        UserInteraction<ThirdPartyAppInfo> thirdPartyAppUserInteraction =
                buildThirdPartyApp(response.getConsentAppUrl(), state);

        return IntermediateStepResponse.nextStep(this.handleCallbackDataStep)
                .userInteraction(thirdPartyAppUserInteraction)
                .build();
    }

    private UserInteraction<ThirdPartyAppInfo> buildThirdPartyApp(URL consentAppUrl, String state) {
        ThirdPartyAppInfo thirdPartyAppInfo = ThirdPartyAppInfo.of(consentAppUrl);
        return UserInteraction.thirdPartyApp(thirdPartyAppInfo).userResponseRequired(state).build();
    }
}
