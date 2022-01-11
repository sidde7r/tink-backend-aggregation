package se.tink.agent.runtime.authentication.processes.oauth2_decoupled_app;

import java.util.Optional;
import se.tink.agent.runtime.authentication.processes.AuthenticationProcess;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.authentication.authenticators.oauth2.steps.Oauth2ValidateOrRefreshAccessTokenStep;
import se.tink.agent.sdk.authentication.authenticators.oauth2_decoupled_app.Oauth2DecoupledAppAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.oauth2_decoupled_app.steps.Oauth2FetchAccessToken;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppInitStep;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppOpenAppStep;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppPollStep;
import se.tink.agent.sdk.authentication.common_steps.VerifyBankConnectionStep;
import se.tink.agent.sdk.authentication.consent.ConsentLifetime;
import se.tink.agent.sdk.authentication.consent.ConsentStatus;
import se.tink.agent.sdk.authentication.features.AuthenticateOauth2DecoupledApp;
import se.tink.agent.sdk.steppable_execution.execution_flow.InteractiveExecutionFlow;
import se.tink.agent.sdk.steppable_execution.execution_flow.NonInteractiveExecutionFlow;
import se.tink.agent.sdk.utils.Sleeper;

public class Oauth2DecoupledAppAuthenticationProcess
        implements AuthenticationProcess<Oauth2DecoupledAppAuthenticator> {

    private final Sleeper sleeper;

    public Oauth2DecoupledAppAuthenticationProcess(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    @Override
    public Optional<Oauth2DecoupledAppAuthenticator> tryInstantiateAuthenticator(
            AgentInstance agentInstance) {
        return agentInstance
                .instanceOf(AuthenticateOauth2DecoupledApp.class)
                .map(AuthenticateOauth2DecoupledApp::authenticator);
    }

    @Override
    public InteractiveExecutionFlow<ConsentLifetime> getNewConsentFlow(
            Oauth2DecoupledAppAuthenticator authenticator) {
        return InteractiveExecutionFlow.<ConsentLifetime>startStep(
                        new ThirdPartyAppInitStep(authenticator, ThirdPartyAppOpenAppStep.class))
                .addStep(new ThirdPartyAppOpenAppStep(authenticator, ThirdPartyAppPollStep.class))
                .addStep(
                        new ThirdPartyAppPollStep(
                                authenticator,
                                this.sleeper,
                                ThirdPartyAppOpenAppStep.class,
                                Oauth2FetchAccessToken.class))
                .addStep(new Oauth2FetchAccessToken(authenticator))
                .build();
    }

    @Override
    public NonInteractiveExecutionFlow<ConsentStatus> getUseExistingConsentFlow(
            Oauth2DecoupledAppAuthenticator authenticator) {
        return NonInteractiveExecutionFlow.startStep(
                        new Oauth2ValidateOrRefreshAccessTokenStep(
                                authenticator, VerifyBankConnectionStep.class))
                .addStep(new VerifyBankConnectionStep(authenticator))
                .build();
    }
}
