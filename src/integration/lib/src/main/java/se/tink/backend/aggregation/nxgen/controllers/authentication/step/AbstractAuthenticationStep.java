package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import java.util.Objects;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractAuthenticationStep that = (AbstractAuthenticationStep) o;
        return Objects.equals(getIdentifier(), that.getIdentifier());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdentifier());
    }
}
