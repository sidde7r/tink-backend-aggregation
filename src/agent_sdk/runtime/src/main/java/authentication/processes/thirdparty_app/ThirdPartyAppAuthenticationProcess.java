package se.tink.agent.runtime.authentication.processes.thirdparty_app;

import java.util.Optional;
import se.tink.agent.runtime.authentication.processes.AuthenticationProcess;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.authentication.AuthenticationFlow;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppInitStep;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppOpenAppStep;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppPollStep;
import se.tink.agent.sdk.authentication.capability.AuthenticateThirdPartyApp;
import se.tink.agent.sdk.authentication.common_steps.GetConsentLifetimeStep;
import se.tink.agent.sdk.authentication.common_steps.VerifyBankConnectionStep;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentStep;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;
import se.tink.agent.sdk.utils.Sleeper;

public class ThirdPartyAppAuthenticationProcess
        implements AuthenticationProcess<ThirdPartyAppAuthenticator> {
    @Override
    public Optional<ThirdPartyAppAuthenticator> instantiateAuthenticator(
            AgentInstance agentInstance) {
        return agentInstance
                .instanceOf(AuthenticateThirdPartyApp.class)
                .map(AuthenticateThirdPartyApp::thirdPartyAppAuthenticator);
    }

    @Override
    public AuthenticationFlow<NewConsentStep> getNewConsentFlow(
            ThirdPartyAppAuthenticator authenticator) {
        Sleeper sleeper = null;
        return AuthenticationFlow.builder(
                        new ThirdPartyAppInitStep(authenticator, ThirdPartyAppOpenAppStep.class))
                .addStep(new ThirdPartyAppOpenAppStep(authenticator, ThirdPartyAppPollStep.class))
                .addStep(
                        new ThirdPartyAppPollStep(
                                authenticator,
                                sleeper,
                                ThirdPartyAppOpenAppStep.class,
                                GetConsentLifetimeStep.class))
                .addStep(new GetConsentLifetimeStep(authenticator))
                .build();
    }

    @Override
    public AuthenticationFlow<ExistingConsentStep> getUseExistingConsentFlow(
            ThirdPartyAppAuthenticator authenticator) {
        return AuthenticationFlow.builder(new VerifyBankConnectionStep(authenticator)).build();
    }
}
