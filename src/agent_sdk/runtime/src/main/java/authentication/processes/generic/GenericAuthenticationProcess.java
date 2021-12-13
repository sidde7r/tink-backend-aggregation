package se.tink.agent.runtime.authentication.processes.generic;

import java.util.Optional;
import se.tink.agent.runtime.authentication.processes.AuthenticationProcess;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.authentication.authenticators.generic.AuthenticationFlow;
import se.tink.agent.sdk.authentication.authenticators.generic.GenericAuthenticator;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentStep;
import se.tink.agent.sdk.authentication.features.AuthenticateGeneric;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;

public class GenericAuthenticationProcess implements AuthenticationProcess<GenericAuthenticator> {
    @Override
    public Optional<GenericAuthenticator> tryInstantiateAuthenticator(AgentInstance agentInstance) {
        return agentInstance
                .instanceOf(AuthenticateGeneric.class)
                .map(AuthenticateGeneric::authenticator);
    }

    @Override
    public AuthenticationFlow<NewConsentStep> getNewConsentFlow(
            GenericAuthenticator authenticator) {
        return authenticator.issueNewConsent();
    }

    @Override
    public AuthenticationFlow<ExistingConsentStep> getUseExistingConsentFlow(
            GenericAuthenticator authenticator) {
        return authenticator.useExistingConsent();
    }
}
