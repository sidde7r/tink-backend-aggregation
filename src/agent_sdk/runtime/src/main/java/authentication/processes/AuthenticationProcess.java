package se.tink.agent.runtime.authentication.processes;

import java.util.Optional;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.authentication.steppable_execution.ExistingConsentFlow;
import se.tink.agent.sdk.authentication.steppable_execution.NewConsentFlow;

public interface AuthenticationProcess<T> {
    Optional<T> tryInstantiateAuthenticator(AgentInstance agentInstance);

    NewConsentFlow getNewConsentFlow(T authenticator);

    ExistingConsentFlow getUseExistingConsentFlow(T authenticator);
}
