package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;

@RequiredArgsConstructor
@Getter
public enum ExecutorSignStep {
    AUTHENTICATE,
    CHECK_STATUS;

    public static ExecutorSignStep of(String value) {
        if (SigningStepConstants.STEP_INIT.equals(value)) {
            return AUTHENTICATE;
        }

        return ExecutorSignStep.valueOf(value);
    }
}
