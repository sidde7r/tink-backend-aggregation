package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold.RequestBasedMultiStepsProcess;

@RequiredArgsConstructor
public class RegistrationProcess extends RequestBasedMultiStepsProcess {

    private final LoginStep loginStep;

    public void registerSteps() {
        registerInitialStep(loginStep);
    }
}
