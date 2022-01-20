package se.tink.agent.runtime.authentication.processes.thirdparty_app;

import java.util.Optional;
import se.tink.agent.runtime.authentication.processes.AuthenticationProcess;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppInitStep;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppOpenAppStep;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppPollStep;
import se.tink.agent.sdk.authentication.common_steps.GetConsentLifetimeStep;
import se.tink.agent.sdk.authentication.common_steps.VerifyBankConnectionStep;
import se.tink.agent.sdk.authentication.features.AuthenticateThirdPartyApp;
import se.tink.agent.sdk.authentication.steppable_execution.ExistingConsentFlow;
import se.tink.agent.sdk.authentication.steppable_execution.NewConsentFlow;
import se.tink.agent.sdk.utils.Sleeper;

public class ThirdPartyAppAuthenticationProcess
        implements AuthenticationProcess<ThirdPartyAppAuthenticator> {

    private final Sleeper sleeper;

    public ThirdPartyAppAuthenticationProcess(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    @Override
    public Optional<ThirdPartyAppAuthenticator> tryInstantiateAuthenticator(
            AgentInstance agentInstance) {
        return agentInstance
                .instanceOf(AuthenticateThirdPartyApp.class)
                .map(AuthenticateThirdPartyApp::authenticator);
    }

    @Override
    public NewConsentFlow getNewConsentFlow(ThirdPartyAppAuthenticator authenticator) {
        return NewConsentFlow.builder()
                .startStep(new ThirdPartyAppInitStep(authenticator, ThirdPartyAppOpenAppStep.class))
                .addStep(new ThirdPartyAppOpenAppStep(authenticator, ThirdPartyAppPollStep.class))
                .addStep(
                        new ThirdPartyAppPollStep(
                                authenticator,
                                this.sleeper,
                                ThirdPartyAppOpenAppStep.class,
                                GetConsentLifetimeStep.class))
                .addStep(new GetConsentLifetimeStep(authenticator))
                .build();
    }

    @Override
    public ExistingConsentFlow getUseExistingConsentFlow(ThirdPartyAppAuthenticator authenticator) {
        return ExistingConsentFlow.builder()
                .startStep(new VerifyBankConnectionStep(authenticator))
                .build();
    }
}
