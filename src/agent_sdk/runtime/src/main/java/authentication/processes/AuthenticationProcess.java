package se.tink.agent.runtime.authentication.processes;

import java.util.Optional;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.authentication.AuthenticationFlow;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentStep;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;

public interface AuthenticationProcess<T> {
    Optional<T> instantiateAuthenticator(AgentInstance agentInstance);

    AuthenticationFlow<NewConsentStep> getNewConsentFlow(T authenticator);

    AuthenticationFlow<ExistingConsentStep> getUseExistingConsentFlow(T authenticator);
}
