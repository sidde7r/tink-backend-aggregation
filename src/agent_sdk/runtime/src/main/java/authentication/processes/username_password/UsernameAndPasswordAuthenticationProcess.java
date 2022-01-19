package se.tink.agent.runtime.authentication.processes.username_password;

import java.util.Optional;
import se.tink.agent.runtime.authentication.processes.AuthenticationProcess;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.authentication.authenticators.username_password.UsernameAndPasswordAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.username_password.steps.UsernameAndPasswordStep;
import se.tink.agent.sdk.authentication.common_steps.VerifyBankConnectionStep;
import se.tink.agent.sdk.authentication.consent.ConsentLifetime;
import se.tink.agent.sdk.authentication.consent.ConsentStatus;
import se.tink.agent.sdk.authentication.features.AuthenticateUsernameAndPassword;
import se.tink.agent.sdk.operation.StaticBankCredentials;
import se.tink.agent.sdk.steppable_execution.execution_flow.InteractiveExecutionFlow;
import se.tink.agent.sdk.steppable_execution.execution_flow.NonInteractiveExecutionFlow;

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
    public InteractiveExecutionFlow<Void, ConsentLifetime> getNewConsentFlow(
            UsernameAndPasswordAuthenticator authenticator) {
        return InteractiveExecutionFlow.startStep(
                        new UsernameAndPasswordStep(this.staticBankCredentials, authenticator))
                .build();
    }

    @Override
    public NonInteractiveExecutionFlow<Void, ConsentStatus> getUseExistingConsentFlow(
            UsernameAndPasswordAuthenticator authenticator) {
        return NonInteractiveExecutionFlow.startStep(new VerifyBankConnectionStep(authenticator))
                .build();
    }
}
