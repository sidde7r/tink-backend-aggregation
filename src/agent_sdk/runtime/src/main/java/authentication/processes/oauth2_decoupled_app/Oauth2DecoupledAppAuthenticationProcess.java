package se.tink.agent.runtime.authentication.processes.oauth2_decoupled_app;

import java.util.Optional;
import se.tink.agent.runtime.authentication.processes.AuthenticationProcess;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.authentication.AuthenticationFlow;
import se.tink.agent.sdk.authentication.authenticators.oauth2.steps.Oauth2ValidateOrRefreshAccessTokenStep;
import se.tink.agent.sdk.authentication.authenticators.oauth2_decoupled_app.Oauth2DecoupledAppAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.oauth2_decoupled_app.steps.Oauth2FetchAccessToken;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppInitStep;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppOpenAppStep;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.steps.ThirdPartyAppPollStep;
import se.tink.agent.sdk.authentication.capability.AuthenticateOauth2DecoupledApp;
import se.tink.agent.sdk.authentication.common_steps.VerifyBankConnectionStep;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentStep;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;
import se.tink.agent.sdk.utils.Sleeper;

public class Oauth2DecoupledAppAuthenticationProcess
        implements AuthenticationProcess<Oauth2DecoupledAppAuthenticator> {
    @Override
    public Optional<Oauth2DecoupledAppAuthenticator> instantiateAuthenticator(
            AgentInstance agentInstance) {
        return agentInstance
                .instanceOf(AuthenticateOauth2DecoupledApp.class)
                .map(AuthenticateOauth2DecoupledApp::oauth2DecoupledAppAuthenticator);
    }

    @Override
    public AuthenticationFlow<NewConsentStep> getNewConsentFlow(
            Oauth2DecoupledAppAuthenticator authenticator) {
        Sleeper sleeper = null;

        return AuthenticationFlow.builder(
                        new ThirdPartyAppInitStep(authenticator, ThirdPartyAppOpenAppStep.class))
                .addStep(new ThirdPartyAppOpenAppStep(authenticator, ThirdPartyAppPollStep.class))
                .addStep(
                        new ThirdPartyAppPollStep(
                                authenticator,
                                sleeper,
                                ThirdPartyAppOpenAppStep.class,
                                Oauth2FetchAccessToken.class))
                .addStep(new Oauth2FetchAccessToken(authenticator))
                .build();
    }

    @Override
    public AuthenticationFlow<ExistingConsentStep> getUseExistingConsentFlow(
            Oauth2DecoupledAppAuthenticator authenticator) {
        return AuthenticationFlow.builder(
                        new Oauth2ValidateOrRefreshAccessTokenStep(
                                authenticator, VerifyBankConnectionStep.class))
                .addStep(new VerifyBankConnectionStep(authenticator))
                .build();
    }
}
