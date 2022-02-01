package src.agent_sdk.runtime.src.authentication.processes.username_password;

import java.util.Optional;
import se.tink.agent.sdk.authentication.authenticators.username_password.UsernameAndPasswordAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.username_password.steps.UsernameAndPasswordStep;
import se.tink.agent.sdk.authentication.common_steps.VerifyBankConnectionStep;
import se.tink.agent.sdk.authentication.features.AuthenticateUsernameAndPassword;
import se.tink.agent.sdk.authentication.steppable_execution.ExistingConsentFlow;
import se.tink.agent.sdk.authentication.steppable_execution.NewConsentFlow;
import se.tink.agent.sdk.operation.StaticBankCredentials;
import src.agent_sdk.runtime.src.authentication.processes.AuthenticationProcess;
import src.agent_sdk.runtime.src.instance.AgentInstance;

public class UsernameAndPasswordAuthenticationProcess
        implements AuthenticationProcess<UsernameAndPasswordAuthenticator> {

    private final StaticBankCredentials staticBankCredentials;

    public UsernameAndPasswordAuthenticationProcess(StaticBankCredentials staticBankCredentials) {
        this.staticBankCredentials = staticBankCredentials;
    }

    @Override
    public Optional<UsernameAndPasswordAuthenticator> tryInstantiateAuthenticator(
            AgentInstance agentInstance) {
        return agentInstance
                .instanceOf(AuthenticateUsernameAndPassword.class)
                .map(AuthenticateUsernameAndPassword::authenticator);
    }

    @Override
    public NewConsentFlow getNewConsentFlow(UsernameAndPasswordAuthenticator authenticator) {
        return NewConsentFlow.builder()
                .startStep(new UsernameAndPasswordStep(this.staticBankCredentials, authenticator))
                .build();
    }

    @Override
    public ExistingConsentFlow getUseExistingConsentFlow(
            UsernameAndPasswordAuthenticator authenticator) {
        return ExistingConsentFlow.builder()
                .startStep(new VerifyBankConnectionStep(authenticator))
                .build();
    }
}
