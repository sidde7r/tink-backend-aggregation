package se.tink.agent.sdk.authentication.authenticators.generic;

import se.tink.agent.sdk.authentication.consent.ConsentLifetime;
import se.tink.agent.sdk.authentication.consent.ConsentStatus;
import se.tink.agent.sdk.steppable_execution.execution_flow.InteractiveExecutionFlow;
import se.tink.agent.sdk.steppable_execution.execution_flow.NonInteractiveExecutionFlow;

public interface GenericAuthenticator {
    // "full authentication"
    InteractiveExecutionFlow<ConsentLifetime> issueNewConsent();

    // "is logged in"
    NonInteractiveExecutionFlow<ConsentStatus> useExistingConsent();
}
