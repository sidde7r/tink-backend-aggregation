package se.tink.agent.runtime.authentication.processes;

import java.util.Optional;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.authentication.consent.ConsentLifetime;
import se.tink.agent.sdk.authentication.consent.ConsentStatus;
import se.tink.agent.sdk.steppable_execution.execution_flow.InteractiveExecutionFlow;
import se.tink.agent.sdk.steppable_execution.execution_flow.NonInteractiveExecutionFlow;

public interface AuthenticationProcess<T> {
    Optional<T> tryInstantiateAuthenticator(AgentInstance agentInstance);

    InteractiveExecutionFlow<Void, ConsentLifetime> getNewConsentFlow(T authenticator);

    NonInteractiveExecutionFlow<Void, ConsentStatus> getUseExistingConsentFlow(T authenticator);
}
