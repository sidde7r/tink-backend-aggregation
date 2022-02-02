package src.agent_sdk.runtime.src.authentication.processes;

import java.util.Optional;
import se.tink.agent.sdk.authentication.steppable_execution.ExistingConsentFlow;
import se.tink.agent.sdk.authentication.steppable_execution.NewConsentFlow;
import src.agent_sdk.runtime.src.instance.AgentInstance;

public interface AuthenticationProcess<T> {
    Optional<T> tryInstantiateAuthenticator(AgentInstance agentInstance);

    NewConsentFlow getNewConsentFlow(T authenticator);

    ExistingConsentFlow getUseExistingConsentFlow(T authenticator);
}
