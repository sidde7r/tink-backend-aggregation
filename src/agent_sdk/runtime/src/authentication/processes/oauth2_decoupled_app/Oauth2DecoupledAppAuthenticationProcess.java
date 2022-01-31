package src.agent_sdk.runtime.src.authentication.processes.oauth2_decoupled_app;

import java.util.Optional;
import se.tink.agent.sdk.authentication.authenticators.oauth2.steps.Oauth2ValidateOrRefreshAccessTokenStep;
import se.tink.agent.sdk.authentication.authenticators.oauth2_decoupled_app.Oauth2DecoupledAppAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.oauth2_decoupled_app.steps.Oauth2FetchAccessToken;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppInitStep;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppOpenAppStep;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppPollStep;
import se.tink.agent.sdk.authentication.common_steps.VerifyBankConnectionStep;
import se.tink.agent.sdk.authentication.features.AuthenticateOauth2DecoupledApp;
import se.tink.agent.sdk.authentication.steppable_execution.ExistingConsentFlow;
import se.tink.agent.sdk.authentication.steppable_execution.NewConsentFlow;
import se.tink.agent.sdk.utils.Sleeper;
import src.agent_sdk.runtime.src.authentication.processes.AuthenticationProcess;
import src.agent_sdk.runtime.src.instance.AgentInstance;

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
    public NewConsentFlow getNewConsentFlow(Oauth2DecoupledAppAuthenticator authenticator) {
        return NewConsentFlow.builder()
                .startStep(new ThirdPartyAppInitStep(authenticator, ThirdPartyAppOpenAppStep.class))
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
    public ExistingConsentFlow getUseExistingConsentFlow(
            Oauth2DecoupledAppAuthenticator authenticator) {
        return ExistingConsentFlow.builder()
                .startStep(
                        new Oauth2ValidateOrRefreshAccessTokenStep(
                                authenticator, VerifyBankConnectionStep.class))
                .addStep(new VerifyBankConnectionStep(authenticator))
                .build();
    }
}
