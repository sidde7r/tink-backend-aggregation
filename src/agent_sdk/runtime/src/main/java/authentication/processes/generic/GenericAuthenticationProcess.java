package se.tink.agent.runtime.authentication.processes.generic;

import java.util.Optional;
import se.tink.agent.runtime.authentication.processes.AuthenticationProcess;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.authentication.authenticators.generic.GenericAuthenticator;
import se.tink.agent.sdk.authentication.consent.ConsentLifetime;
import se.tink.agent.sdk.authentication.consent.ConsentStatus;
import se.tink.agent.sdk.authentication.features.AuthenticateGeneric;
import se.tink.agent.sdk.steppable_execution.execution_flow.InteractiveExecutionFlow;
import se.tink.agent.sdk.steppable_execution.execution_flow.NonInteractiveExecutionFlow;

public class GenericAuthenticationProcess implements AuthenticationProcess<GenericAuthenticator> {
    @Override
    public Optional<GenericAuthenticator> tryInstantiateAuthenticator(AgentInstance agentInstance) {
        return agentInstance
                .instanceOf(AuthenticateGeneric.class)
                .map(AuthenticateGeneric::authenticator);
    }

    @Override
    public InteractiveExecutionFlow<ConsentLifetime> getNewConsentFlow(
            GenericAuthenticator authenticator) {
        return authenticator.issueNewConsent();
    }

    @Override
    public NonInteractiveExecutionFlow<ConsentStatus> getUseExistingConsentFlow(
            GenericAuthenticator authenticator) {
        return authenticator.useExistingConsent();
    }
}
