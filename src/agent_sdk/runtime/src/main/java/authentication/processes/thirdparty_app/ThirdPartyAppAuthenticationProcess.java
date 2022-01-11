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
import se.tink.agent.sdk.authentication.consent.ConsentLifetime;
import se.tink.agent.sdk.authentication.consent.ConsentStatus;
import se.tink.agent.sdk.authentication.features.AuthenticateThirdPartyApp;
import se.tink.agent.sdk.steppable_execution.execution_flow.InteractiveExecutionFlow;
import se.tink.agent.sdk.steppable_execution.execution_flow.NonInteractiveExecutionFlow;
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
    public InteractiveExecutionFlow<ConsentLifetime> getNewConsentFlow(
            ThirdPartyAppAuthenticator authenticator) {
        return InteractiveExecutionFlow.<ConsentLifetime>startStep(
                        new ThirdPartyAppInitStep(authenticator, ThirdPartyAppOpenAppStep.class))
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
    public NonInteractiveExecutionFlow<ConsentStatus> getUseExistingConsentFlow(
            ThirdPartyAppAuthenticator authenticator) {
        return NonInteractiveExecutionFlow.startStep(new VerifyBankConnectionStep(authenticator))
                .build();
    }
}
