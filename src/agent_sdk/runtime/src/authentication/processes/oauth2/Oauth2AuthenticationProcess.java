package se.tink.agent.runtime.authentication.processes.oauth2;

import java.util.Optional;
import se.tink.agent.runtime.authentication.processes.AuthenticationProcess;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.authentication.authenticators.oauth2.Oauth2Authenticator;
import se.tink.agent.sdk.authentication.authenticators.oauth2.steps.Oauth2ExchangeAuthorizationCodeStep;
import se.tink.agent.sdk.authentication.authenticators.oauth2.steps.Oauth2OpenAuthorizationAppStep;
import se.tink.agent.sdk.authentication.authenticators.oauth2.steps.Oauth2ValidateOrRefreshAccessTokenStep;
import se.tink.agent.sdk.authentication.common_steps.VerifyBankConnectionStep;
import se.tink.agent.sdk.authentication.features.AuthenticateOauth2;
import se.tink.agent.sdk.authentication.steppable_execution.ExistingConsentFlow;
import se.tink.agent.sdk.authentication.steppable_execution.NewConsentFlow;
import se.tink.agent.sdk.utils.RandomGenerator;

public class Oauth2AuthenticationProcess implements AuthenticationProcess<Oauth2Authenticator> {
    private final RandomGenerator randomGenerator;

    public Oauth2AuthenticationProcess(RandomGenerator randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    @Override
    public Optional<Oauth2Authenticator> tryInstantiateAuthenticator(AgentInstance agentInstance) {
        return agentInstance
                .instanceOf(AuthenticateOauth2.class)
                .map(AuthenticateOauth2::authenticator);
    }

    @Override
    public NewConsentFlow getNewConsentFlow(Oauth2Authenticator authenticator) {
        return NewConsentFlow.builder()
                .startStep(
                        new Oauth2OpenAuthorizationAppStep(
                                this.randomGenerator,
                                authenticator,
                                Oauth2ExchangeAuthorizationCodeStep.class))
                .addStep(new Oauth2ExchangeAuthorizationCodeStep(authenticator, authenticator))
                .build();
    }

    @Override
    public ExistingConsentFlow getUseExistingConsentFlow(Oauth2Authenticator authenticator) {
        return ExistingConsentFlow.builder()
                .startStep(
                        new Oauth2ValidateOrRefreshAccessTokenStep(
                                authenticator, VerifyBankConnectionStep.class))
                .addStep(new VerifyBankConnectionStep(authenticator))
                .build();
    }
}
