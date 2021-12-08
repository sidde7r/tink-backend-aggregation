package se.tink.agent.runtime.authentication.processes.swedish_mobile_bankid;

import java.util.Optional;
import se.tink.agent.runtime.authentication.processes.AuthenticationProcess;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.authentication.authenticators.generic.AuthenticationFlow;
import se.tink.agent.sdk.authentication.authenticators.swedish_mobile_bankid.SwedishMobileBankIdAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.swedish_mobile_bankid.steps.SwedishMobileBankIdOpenAppStep;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppInitStep;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppPollStep;
import se.tink.agent.sdk.authentication.common_steps.GetConsentLifetimeStep;
import se.tink.agent.sdk.authentication.common_steps.VerifyBankConnectionStep;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentStep;
import se.tink.agent.sdk.authentication.features.AuthenticateSwedishMobileBankId;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;
import se.tink.agent.sdk.utils.Sleeper;

public class SwedishMobileBankIdAuthenticationProcess
        implements AuthenticationProcess<SwedishMobileBankIdAuthenticator> {

    @Override
    public Optional<SwedishMobileBankIdAuthenticator> instantiateAuthenticator(
            AgentInstance agentInstance) {
        return agentInstance
                .instanceOf(AuthenticateSwedishMobileBankId.class)
                .map(AuthenticateSwedishMobileBankId::authenticator);
    }

    @Override
    public AuthenticationFlow<NewConsentStep> getNewConsentFlow(
            SwedishMobileBankIdAuthenticator authenticator) {
        Sleeper sleeper = null;
        return AuthenticationFlow.builder(
                        new ThirdPartyAppInitStep(
                                authenticator, SwedishMobileBankIdOpenAppStep.class))
                .addStep(
                        new SwedishMobileBankIdOpenAppStep(
                                authenticator, ThirdPartyAppPollStep.class))
                .addStep(
                        new ThirdPartyAppPollStep(
                                authenticator,
                                sleeper,
                                SwedishMobileBankIdOpenAppStep.class,
                                GetConsentLifetimeStep.class))
                .addStep(new GetConsentLifetimeStep(authenticator))
                .build();
    }

    @Override
    public AuthenticationFlow<ExistingConsentStep> getUseExistingConsentFlow(
            SwedishMobileBankIdAuthenticator authenticator) {
        return AuthenticationFlow.builder(new VerifyBankConnectionStep(authenticator)).build();
    }
}
