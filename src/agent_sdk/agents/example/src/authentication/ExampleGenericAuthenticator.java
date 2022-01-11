package se.tink.agent.agents.example.authentication;

import se.tink.agent.sdk.authentication.authenticators.generic.GenericAuthenticator;
import se.tink.agent.sdk.authentication.consent.ConsentLifetime;
import se.tink.agent.sdk.authentication.consent.ConsentStatus;
import se.tink.agent.sdk.steppable_execution.execution_flow.InteractiveExecutionFlow;
import se.tink.agent.sdk.steppable_execution.execution_flow.NonInteractiveExecutionFlow;

public class ExampleGenericAuthenticator implements GenericAuthenticator {

    @Override
    public InteractiveExecutionFlow<ConsentLifetime> issueNewConsent() {
        return null;
    }

    @Override
    public NonInteractiveExecutionFlow<ConsentStatus> useExistingConsent() {
        return null;
    }
}
