package se.tink.agent.runtime.authentication.processes.oauth2;

import java.util.Optional;
import se.tink.agent.runtime.authentication.processes.AuthenticationProcess;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.authentication.AuthenticationFlow;
import se.tink.agent.sdk.authentication.authenticators.oauth2.Oauth2Authenticator;
import se.tink.agent.sdk.authentication.authenticators.oauth2.steps.Oauth2ExchangeAuthorizationCodeStep;
import se.tink.agent.sdk.authentication.authenticators.oauth2.steps.Oauth2OpenAuthorizationAppStep;
import se.tink.agent.sdk.authentication.authenticators.oauth2.steps.Oauth2ValidateOrRefreshAccessTokenStep;
import se.tink.agent.sdk.authentication.capability.AuthenticateOauth2;
import se.tink.agent.sdk.authentication.common_steps.VerifyBankConnectionStep;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentStep;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;
import se.tink.agent.sdk.environment.MultifactorAuthenticationState;

public class Oauth2AuthenticationProcess implements AuthenticationProcess<Oauth2Authenticator> {
    private final MultifactorAuthenticationState multifactorAuthenticationState;

    public Oauth2AuthenticationProcess(
            MultifactorAuthenticationState multifactorAuthenticationState) {
        this.multifactorAuthenticationState = multifactorAuthenticationState;
    }

    @Override
    public Optional<Oauth2Authenticator> instantiateAuthenticator(AgentInstance agentInstance) {
        return agentInstance
                .instanceOf(AuthenticateOauth2.class)
                .map(AuthenticateOauth2::oauth2Authenticator);
    }

    @Override
    public AuthenticationFlow<NewConsentStep> getNewConsentFlow(Oauth2Authenticator authenticator) {
        return AuthenticationFlow.builder(
                        new Oauth2OpenAuthorizationAppStep(
                                this.multifactorAuthenticationState,
                                authenticator,
                                Oauth2ExchangeAuthorizationCodeStep.class))
                .addStep(new Oauth2ExchangeAuthorizationCodeStep(authenticator, authenticator))
                .build();
    }

    @Override
    public AuthenticationFlow<ExistingConsentStep> getUseExistingConsentFlow(
            Oauth2Authenticator authenticator) {
        return AuthenticationFlow.builder(
                        new Oauth2ValidateOrRefreshAccessTokenStep(
                                authenticator, VerifyBankConnectionStep.class))
                .addStep(new VerifyBankConnectionStep(authenticator))
                .build();
    }
}
