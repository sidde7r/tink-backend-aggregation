package se.tink.agent.runtime.authentication.processes.generic;

import java.util.Optional;
import se.tink.agent.runtime.authentication.processes.AuthenticationProcess;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.authentication.authenticators.generic.GenericAuthenticator;
import se.tink.agent.sdk.authentication.features.AuthenticateGeneric;
import se.tink.agent.sdk.authentication.steppable_execution.ExistingConsentFlow;
import se.tink.agent.sdk.authentication.steppable_execution.NewConsentFlow;

public class GenericAuthenticationProcess implements AuthenticationProcess<GenericAuthenticator> {
    @Override
    public Optional<GenericAuthenticator> tryInstantiateAuthenticator(AgentInstance agentInstance) {
        return agentInstance
                .instanceOf(AuthenticateGeneric.class)
                .map(AuthenticateGeneric::authenticator);
    }

    @Override
    public NewConsentFlow getNewConsentFlow(GenericAuthenticator authenticator) {
        return authenticator.issueNewConsent();
    }

    @Override
    public ExistingConsentFlow getUseExistingConsentFlow(GenericAuthenticator authenticator) {
        return authenticator.useExistingConsent();
    }
}
