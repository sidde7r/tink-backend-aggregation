package se.tink.agent.sdk.authentication.steppable_execution;

import se.tink.agent.sdk.authentication.consent.ConsentLifetime;
import se.tink.agent.sdk.steppable_execution.interactive_step.InteractiveStep;

public abstract class NewConsentStep extends InteractiveStep<Void, ConsentLifetime> {}
