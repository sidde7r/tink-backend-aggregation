package se.tink.agent.sdk.authentication.steppable_execution;

import se.tink.agent.sdk.authentication.consent.ConsentStatus;
import se.tink.agent.sdk.steppable_execution.non_interactive_step.NonInteractiveStep;

public abstract class ExistingConsentStep extends NonInteractiveStep<Void, ConsentStatus> {}
