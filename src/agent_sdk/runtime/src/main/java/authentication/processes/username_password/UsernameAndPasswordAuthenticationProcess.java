package se.tink.agent.runtime.authentication.processes.username_password;

import java.util.Optional;
import se.tink.agent.runtime.authentication.processes.AuthenticationProcess;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.authentication.AuthenticationFlow;
import se.tink.agent.sdk.authentication.authenticators.username_password.UsernameAndPasswordAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.username_password.steps.UsernameAndPasswordStep;
import se.tink.agent.sdk.authentication.capability.AuthenticateUsernameAndPassword;
import se.tink.agent.sdk.authentication.common_steps.VerifyBankConnectionStep;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentStep;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;

public class UsernameAndPasswordAuthenticationProcess
        implements AuthenticationProcess<UsernameAndPasswordAuthenticator> {
    @Override
    public Optional<UsernameAndPasswordAuthenticator> instantiateAuthenticator(
            AgentInstance agentInstance) {
        return agentInstance
                .instanceOf(AuthenticateUsernameAndPassword.class)
                .map(AuthenticateUsernameAndPassword::authenticator);
    }

    @Override
    public AuthenticationFlow<NewConsentStep> getNewConsentFlow(
            UsernameAndPasswordAuthenticator authenticator) {
        return AuthenticationFlow.builder(new UsernameAndPasswordStep(authenticator)).build();
    }

    @Override
    public AuthenticationFlow<ExistingConsentStep> getUseExistingConsentFlow(
            UsernameAndPasswordAuthenticator authenticator) {
        return AuthenticationFlow.builder(new VerifyBankConnectionStep(authenticator)).build();
    }
}
