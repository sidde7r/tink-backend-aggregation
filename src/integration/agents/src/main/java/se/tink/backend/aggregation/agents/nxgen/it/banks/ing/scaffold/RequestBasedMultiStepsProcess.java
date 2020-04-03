package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationResponse;

public abstract class RequestBasedMultiStepsProcess implements UserInteractionMultiStepsProcess {

    private final Map<String, UserInteractionStep> steps = new HashMap<>();

    private UserInteractionStep intitialStep;

    @Override
    public SteppableAuthenticationResponse execute(
            SteppableAuthenticationRequest steppableAuthenticationRequest) throws LoginException {
        return steppableAuthenticationRequest
                .getStepIdentifier()
                .map(this::nextStep)
                .orElse(this.intitialStep)
                .execute(steppableAuthenticationRequest);
    }

    protected abstract void registerSteps();

    protected void registerSingleStep(UserInteractionStep step) {
        this.steps.put(step.identifier(), step);
    }

    protected void registerInitialStep(UserInteractionStep step) {
        this.intitialStep = step;
        registerSingleStep(step);
    }

    private UserInteractionStep nextStep(String stepIdentifier) {
        return Optional.of(steps.get(stepIdentifier))
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                "Step with given identifier: '%s' does not register",
                                                stepIdentifier)));
    }
}
