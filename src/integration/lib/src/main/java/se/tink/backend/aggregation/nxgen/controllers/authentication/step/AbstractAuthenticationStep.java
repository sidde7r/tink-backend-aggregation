package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;

public abstract class AbstractAuthenticationStep implements AuthenticationStep {

    private String stepId;

    public AbstractAuthenticationStep() {}

    public AbstractAuthenticationStep(String stepId) {
        this.stepId = stepId;
    }

    @Override
    public String getIdentifier() {
        return stepId != null ? stepId : AuthenticationStep.super.getIdentifier();
    }
}
