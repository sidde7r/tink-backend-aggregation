package se.tink.agent.runtime.authentication.processes.oauth2;

import java.util.Optional;
import se.tink.agent.runtime.authentication.processes.AuthenticationProcess;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.authentication.authenticators.oauth2.Oauth2Authenticator;
import se.tink.agent.sdk.authentication.authenticators.oauth2.steps.Oauth2ExchangeAuthorizationCodeStep;
import se.tink.agent.sdk.authentication.authenticators.oauth2.steps.Oauth2OpenAuthorizationAppStep;
import se.tink.agent.sdk.authentication.authenticators.oauth2.steps.Oauth2ValidateOrRefreshAccessTokenStep;
import se.tink.agent.sdk.authentication.common_steps.VerifyBankConnectionStep;
import se.tink.agent.sdk.authentication.consent.ConsentLifetime;
import se.tink.agent.sdk.authentication.consent.ConsentStatus;
import se.tink.agent.sdk.authentication.features.AuthenticateOauth2;
import se.tink.agent.sdk.operation.MultifactorAuthenticationState;
import se.tink.agent.sdk.steppable_execution.execution_flow.InteractiveExecutionFlow;
import se.tink.agent.sdk.steppable_execution.execution_flow.NonInteractiveExecutionFlow;

public class Oauth2AuthenticationProcess implements AuthenticationProcess<Oauth2Authenticator> {
    private final MultifactorAuthenticationState multifactorAuthenticationState;

    public Oauth2AuthenticationProcess(
            MultifactorAuthenticationState multifactorAuthenticationState) {
        this.multifactorAuthenticationState = multifactorAuthenticationState;
    }

    @Override
    public Optional<Oauth2Authenticator> tryInstantiateAuthenticator(AgentInstance agentInstance) {
        return agentInstance
                .instanceOf(AuthenticateOauth2.class)
                .map(AuthenticateOauth2::authenticator);
    }

    @Override
    public InteractiveExecutionFlow<Void, ConsentLifetime> getNewConsentFlow(
            Oauth2Authenticator authenticator) {
        return InteractiveExecutionFlow.<Void, ConsentLifetime>startStep(
                        new Oauth2OpenAuthorizationAppStep(
                                this.multifactorAuthenticationState,
                                authenticator,
                                Oauth2ExchangeAuthorizationCodeStep.class))
                .addStep(new Oauth2ExchangeAuthorizationCodeStep(authenticator, authenticator))
                .build();
    }

    @Override
    public NonInteractiveExecutionFlow<Void, ConsentStatus> getUseExistingConsentFlow(
            Oauth2Authenticator authenticator) {
        return NonInteractiveExecutionFlow.startStep(
                        new Oauth2ValidateOrRefreshAccessTokenStep(
                                authenticator, VerifyBankConnectionStep.class))
                .addStep(new VerifyBankConnectionStep(authenticator))
                .build();
    }
}
