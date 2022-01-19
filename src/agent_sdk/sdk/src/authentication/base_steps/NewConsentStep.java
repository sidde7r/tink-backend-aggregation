package se.tink.agent.sdk.authentication.base_steps;

import se.tink.agent.sdk.authentication.consent.ConsentLifetime;
import se.tink.agent.sdk.steppable_execution.interactive_step.InteractiveStep;

public abstract class NewConsentStep extends InteractiveStep<Void, ConsentLifetime> {}
